#!/usr/bin/env groovy
pipeline {
    agent any 

	stages{
	    
	    
		stage("Build Dependencies"){
			steps{
				print "Building shared services.."
			    build 'GlobalServices'
				build 'Security'
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
                        env.JAVA_HOME = "/home/ksmitw/java_versions/java_13/jdk-13.0.2"
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

	}
}