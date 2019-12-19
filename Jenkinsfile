
/// !!!Note: this is a reference implementation to show service mesh
// features(templating the values for VirtualService). DO NOT use in production.

def tmpl = readYaml text: """
version: master
global_service_enabled: true
workload_enabled: false 
config_enabled: false
secrets_enabled: false
virtualservice_global:
  hosts:
    - "${params.APP}"
    - "${params.APP}.${params.EXTDOMAIN}"
  headermatch:
  - header: version
    value: ${params.LIVE_VERSION}
    destination: ${params.APP}-${params.LIVE_VERSION}.${params.NS}.${params.INTDOMAIN}
    port: ${params.PORT}
  - header: version
    value: "${env.BRANCH_NAME}-canary"
    destination: ${params.APP}-${env.BRANCH_NAME}-canary.${params.NS}.${params.INTDOMAIN}
    port: ${params.PORT}
  destinations:
  - destination: ${params.APP}-${params.LIVE_VERSION}.${params.NS}.${params.INTDOMAIN}
    weight: 100
    subset: ${params.LIVE_VERSION}
    port: ${params.PORT}
  - destination: ${params.APP}-${env.BRANCH_NAME}-canary.${params.NS}.${params.INTDOMAIN}
    weight: 0
    subset: ${env.BRANCH_NAME}-canary
    port: ${params.PORT}
"""

pipeline {
  parameters {
        string(name: 'APP', defaultValue: 'frontend', description: 'App name')
        //
        string(name: 'PORT', defaultValue: '8080', description: 'Port for the application')
        string(name: 'INTDOMAIN', defaultValue: 'svc.cluster.local', description: 'Internal Domain')
        string(name: 'EXTDOMAIN', defaultValue: 'epo.ss-ops.com', description: 'External Domain')
        string(name: 'LIVE_VERSION', defaultValue: 'master', description: '')
        string(name: 'NS', defaultValue: 'prod', description: '')
        string(name: 'STGNS', defaultValue: 'stage', description: '')
        string(name: 'DEVNS', defaultValue: 'dev', description: '')
        string(name: 'GCP_PROJECT_ID', description: '')
  }

agent {
        kubernetes {
            label 'delivery-pod'
            defaultContainer 'skaffold'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    job: delivery
spec:
  serviceAccountName: jenkins
  containers:
  - name: skaffold
    image: "gcr.io/k8s-skaffold/skaffold:latest"
    command:
    - cat
    tty: true
"""
        }
    }
    options {
        skipDefaultCheckout true
    }

  environment { 
    env = 'qa'
  }

  stages {
    stage('Checkout'){
        steps{
            script{
                checkout scm
            }
        }
    }
    stage('Init Helm') {
      steps {
        sh "env"
        sh "helm init --client-only"
        sh "helm repo add libchart https://raw.githubusercontent.com/CoE-GitHub/libchart/master"
        sh "helm repo update"
      }
    }
    stage('Write version') {
      steps {
        sh """cat skaffold.yaml.tmpl | sed 's@GCP_PROJECT_ID@${params.GCP_PROJECT_ID}@' > skaffold.yaml"""
        sh """ echo "version: ${env.BRANCH_NAME}" > valuesDynamic.yaml """
        //sh """sed -i .bak "s/version:.*/version: ${env.BRANCH_NAME}/g" valuesDynamic.yaml"""
        sh "cat valuesDynamic.yaml"
      }
    }
    stage('Deploy on DEV environment') {
      steps {
        sh "skaffold run"
      }
    }
    stage('Run tests on DEV') {
      steps {
        sh "kubectl wait --for=condition=available --timeout=600s deployment/${params.APP}-${env.BRANCH_NAME} -n ${params.DEVNS}"
        sh "sleep 30 && curl ${params.APP}-${env.BRANCH_NAME}.${params.DEVNS}.${params.INTDOMAIN}:${params.PORT}"
      }
    }
    stage('Deploy on STAGE environment') {
      steps {
        sh "skaffold run -p stage"
      }
    }
    stage('Run tests on STAGE') {
      steps {
        sh "kubectl wait --for=condition=available --timeout=600s deployment/${params.APP}-${env.BRANCH_NAME} -n ${params.STGNS}"
        sh "sleep 30 && curl ${params.APP}-${env.BRANCH_NAME}.${params.STGNS}.${params.INTDOMAIN}:${params.PORT}"
      }
    }
    stage('Deploy on production canary environment') {
      when { anyOf { branch 'master'; } }
      steps {
        sh """ echo "version: ${env.BRANCH_NAME}-canary" > valuesDynamicCanary.yaml """
        sh "skaffold run -p prod-canary"
      }
    }
    stage('Run tests on prod CANARY') {
      when { anyOf { branch 'master'; } }
      steps {
        sh "kubectl wait --for=condition=available --timeout=600s deployment/${params.APP}-${env.BRANCH_NAME}-canary -n ${params.NS}"
        sh "sleep 30 && curl ${params.APP}-${env.BRANCH_NAME}-canary.${params.NS}.${params.INTDOMAIN}:${params.PORT}"
      }
    }
//    stage('Approval canary'){
//      steps {
//        script{
//            input "Continue?"
//        }
//      }
//    }
    stage('10% CANARY') {
      when { anyOf { branch 'master'; } }
      steps {
          sh "rm valuesDynamicCanary.yaml"
          script { 
            tmpl.virtualservice_global.destinations[0].weight = 90
            tmpl.virtualservice_global.destinations[1].weight = 10
            writeYaml file: "valuesDynamicCanary.yaml", data: tmpl
          }
          sh "cat valuesDynamicCanary.yaml"
          sh "helm template charts/${params.APP} -f valuesDynamicCanary.yaml | kubectl -n ${params.NS} apply -f -"
      }
    }
    stage('Deploy on production environment') {
      when { anyOf { branch 'master'; } }
      steps {
        sh """ echo "version: ${env.BRANCH_NAME}" > valuesDynamicProd.yaml """
        sh "skaffold run -p prod"
      }
    }
    stage('Run tests on PROD') {
      when { anyOf { branch 'master'; } }
      steps {
        sh "kubectl wait --for=condition=available --timeout=600s deployment/${params.APP}-${env.BRANCH_NAME} -n ${params.NS}"        
        sh "sleep 30 && curl ${params.APP}-${env.BRANCH_NAME}.${params.NS}.${params.INTDOMAIN}:${params.PORT}"
      }
    }
//    stage('Approval prod'){
//      steps {
//        script{
//            input "Continue?"
//        }
//      }
//    }
    stage('100% PROD') {
      when { anyOf { branch 'master'; } }
      steps {
          sh "rm valuesDynamicProd.yaml"
          script { 
            tmpl.virtualservice_global.destinations[0].weight = 100
            tmpl.virtualservice_global.destinations[1].weight = 0
            writeYaml file: "valuesDynamicProd.yaml", data: tmpl
          }
          sh "cat valuesDynamicProd.yaml"
          sh "helm template charts/${params.APP} -f valuesDynamicCanary.yaml | kubectl -n ${params.NS} apply -f -"
      }
    }

  } //stages
} //pipeline

