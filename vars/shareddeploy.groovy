    def call(Map args) {        

        def deployEnv = args.env
        def towerServer = 'Dev'
        def towerJobId = args.jobId

        def extraVars=""
        script{
            if(args.additionalExtraVars) {
                for(key in args.additionalExtraVars.keySet()){
                    extraVars+=", '${key}':'${args.additionalExtraVars[key]}'"
                }
            }
        }

        echo "Triggering tower deploy: $args.module $args.buildVersion"
        echo "Passed in arguments"
        echo "DEPLOY ENV ${args.env}"
        echo "VERSION ${args.buildVersion}"
        echo "TOWER JOB ID ${args.jobId}"

        ansibleTower (credential: '',
                       importTowerLogs: false,
                       importWorkflowChildLogs: false,
                       inventory: '',
                       jobTags: '',
                       vaultPassword: args.vaultPassword,
                       jobTemplate: towerJobId,
                       limit: '',
                       removeColor: false,
                       templateType: 'job',
                       towerServer: towerServer,
                       verbose: true,
                       extraVars: "{ 'env': ${deployEnv}, 'version': ${args.buildVersion}, 'deployment_iteration': 0, 'aws_region': 'us-east-1', 'wildfly_base_maven_repository': 'snapshots' ${extraVars}}")

    }
