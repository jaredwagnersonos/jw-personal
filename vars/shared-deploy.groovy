    def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
        
        pipeline {
            stages{
          stage('Deploy'){
              steps{
                    lock('ansibleTower') {
                    echo 'Deploying using tower...'

                    echo """curl -k --user ${env.TOWER_ACCESS_USR}:${env.TOWER_ACCESS_PSW} 'https://${config.TOWER_SERVER}/api/v1/job_templates/${config.TOWER_JOB_TPLT_ID}/launch/' -X POST -d '{ "extra_vars": { "env": "${config.DEPLOY_ENV}", "version": "0.0.4-MSSL-SNAPSHOT", "deployment_iteration": 0, "aws_region": "us-east-1" }, "vault_password": "${env.TOWER_VAULT_PWD}" }' -H 'Content-Type:application/json' > tower_launch_output"""

                    sh """curl -k --user ${env.TOWER_ACCESS_USR}:${env.TOWER_ACCESS_PSW} 'https://${config.TOWER_SERVER}/api/v1/job_templates/${config.TOWER_JOB_TPLT_ID}/launch/' -X POST -d '{ "extra_vars": { "env": "${config.DEPLOY_ENV}", "version": "0.0.4-MSSL-SNAPSHOT", "deployment_iteration": 0, "aws_region": "us-east-1" }, "vault_password": "${env.TOWER_VAULT_PWD}" }' -H 'Content-Type:application/json' > tower_launch_output"""
                    echo "Check for launch output"
                    fileExists('tower_launch_output')
                    timeout(time: 40, unit: 'MINUTES') {
                        echo 'Waiting for tower...'
                        script {
                            def towerOutput = readJSON file:'tower_launch_output'
                            towerOutput.each { key, value ->
                            println "${key}: ${value}"
                            }
                            // The .job field was used prior to Ansible Tower 3.0
                            def towerJobId = towerOutput.job ? towerOutput.job : towerOutput.id
                            if (!towerJobId || towerJobId == 'null') {
                                echo 'No Tower Job ID returned - failing deployment!'
                                sh 'exit 1'
                            }
                            echo "Waiting on Tower Job ID: ${towerJobId}..."
                            String status = ''
                            while(status != 'successful' && status != 'failed') {
                                sleep 60 // in secs - check every minute
                                sh """curl -k --user ${env.TOWER_ACCESS_USR}:${env.TOWER_ACCESS_PSW} \
                                      \'https://${config.TOWER_SERVER}/api/v1/jobs/${towerJobId}/\' \
                                      > tower_output"""
                                    towerOutput = readJSON file:'tower_output'
                                    status = towerOutput.status
                                    echo "Tower deployment status: ${status}"
                                }
                            if (status == 'failed') {
                                sh 'exit 1'
                            }
                        }
                    } // timeout 
                  }//lock
              }
          }
          }
        }
    }
