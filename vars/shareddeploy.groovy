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
                       extraVars: "{ 'env': ${deployEnv}, 'wildfly_base_maven_version': ${args.buildVersion}, 'deployment_iteration': 0, 'aws_region': 'us-east-1', 'wildfly_base_maven_repository': 'snapshots' ${extraVars}}")

    }
