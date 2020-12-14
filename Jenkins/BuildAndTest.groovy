#!/usr/bin/env groovy
pipeline {
    agent any 

	stages{
	    
	    
		stage("Build Dependencies"){
			steps{
				print "Building shared services.."
			    build 'GlobalServices'
				build 'Security'
				//This just updates the web test
				build 'ERMWebTest'
			}
		} 
	
		stage("Update project"){
		    steps{
		        script{
		            dir("/var/lib/jenkins/workspace/ERM/FrontEnd"){
        		    sh "git pull https://github.com/LastChance89/ElectricRecordsManager.git master"
		          }
		        }
		    }
		}
		
		stage("Setup Application Properties"){
		    steps{
		      script{
    			   Properties prop = new Properties()
    			   prop.load(new FileInputStream("/var/lib/jenkins/workspace/ERM/FrontEnd/src/main/resources/application.properties"));
    
    			   prop.setProperty("spring.datasource.username", "ac1")
    			   prop.setProperty("spring.datasource.password", "a12gbase41%@!")
    			   prop.setProperty("spring.datasource.url","jdbc:mysql://127.0.0.1:3306/ERM")
    			   prop.setProperty("server.address","localhost")
    			   prop.setProperty("server.port","8080")
    			   prop.store(new FileOutputStream("/var/lib/jenkins/workspace/ERM/FrontEnd/src/main/resources/application.properties"), "");

		      }
		    }
		}

        stage("Execute maven buid"){
            steps{
                script{
                    dir("/var/lib/jenkins/workspace/ERM/FrontEnd"){
                        //Make this property driven somehow
                        env.JAVA_HOME = "/usr/lib/jvm/java-1.13.0-openjdk-amd64"
                        sh "mvn clean install -DskipTests=true -P dev"
                    }
                }
            }
        }
       
        stage("Move War file into run directory"){
            steps{
                sh "cp /var/lib/jenkins/workspace/ERM/FrontEnd/target/FrontEnd-0.0.1-SNAPSHOT.war /home/ksmitw/deployments"
            }
        }
        stage("Update webDriver ini file"){
			steps{
			    script{
			        dir("/var/lib/jenkins/workspace/ERMWebTest/PythonWebDriver/main"){
			            			    sh " python3 SetupConfig.py configuration.ini localhoast 8080"
			        }
			    }

			}
	    }
	    
	    stage("Start server"){
	        steps{
	            script{
	                dir("/home/ksmitw/deployments/scripts"){
	                    sh "nohup  ./start.sh &"
	                      timeout(time: 5, unit: 'MINUTES'){
    	                    sh "./status_check.sh"
    	                }
	                }
	              
	            }
	        }
	    }
	    stage("Execute Selenium Test"){
	        steps{
	            script{
	                dir("/var/lib/jenkins/workspace/ERMWebTest/PythonWebDriver/main"){
	                    //fix this
	                    sh "pip3 install -U selenium"
	                    sh "pip3 install -U  webdriver_manager"
	                    sh "python3 main.py"
	                }
	                
	            }
	        }
	    }
	}
	post{
		always{
			script{
				dir("/home/ksmitw/deployments/scripts"){
					sh "./stop.sh"
				}
			}
		}
		
	}

}