/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rundeck

import com.dtolabs.rundeck.execution.IWorkflowJobItem

/*
* JobExec.java
*
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: Feb 26, 2010 4:49:19 PM
* $Id$
*/

public class JobExec extends WorkflowStep implements IWorkflowJobItem{

    String jobName
    String jobGroup
    String jobProject
    String jobIdentifier
    String argString
    String nodeFilter
    Boolean nodeKeepgoing
    Integer nodeThreadcount
    Boolean nodeStep
    String nodeRankAttribute
    Boolean nodeRankOrderAscending
    Boolean nodeIntersect
    static transients = ['jobIdentifier']

    static constraints = {
        jobName(nullable: false, blank: false, maxSize: 1024)
        jobGroup(nullable: true, blank: true, maxSize: 2048)
        jobProject(nullable: true, blank: true, maxSize: 2048)
        argString(nullable: true, blank: true)
        nodeStep(nullable: true)
        nodeKeepgoing(nullable: true)
        nodeFilter(nullable: true, maxSize: 1024)
        nodeThreadcount(nullable: true)
        nodeRankAttribute(nullable: true, maxSize: 256)
        nodeRankOrderAscending(nullable: true)
        nodeIntersect(nullable: true)
    }

    static mapping = {
        argString type: 'text'
        jobName type: 'string'
        jobGroup type: 'string'
        jobProject type: 'string'
        nodeFilter type: 'text'
        nodeRankAttribute type: 'text'
    }

    public String toString() {
        return "jobref(name=\"${jobName}\" group=\"${jobGroup}\" project=\"${jobProject}\" argString=\"${argString}\" " +
                "nodeStep=\"${nodeStep}\"" +
                "nodeFilter=\"${nodeFilter}\"" +
                "nodeKeepgoing=\"${nodeKeepgoing}\"" +
                "nodeThreadcount=\"${nodeThreadcount}\"" +
                "nodeRankAttribute=\"${nodeRankAttribute}\"" +
                "nodeRankOrderAscending=\"${nodeRankOrderAscending}\"" +
                "nodeIntersect=\"${nodeIntersect}\"" +
                ")" + (errorHandler ? " [handler: ${errorHandler}" : '')
    }

    public String summarize() {
        return "job: ${this.getJobIdentifier()}${argString?' -- '+argString:''}"
    }


    public String getJobIdentifier() {
        return (null==jobGroup?'':jobGroup+"/")+jobName;
    }
    public void setJobIdentifier(){
        //noop
    }

    public JobExec createClone(){
        Map properties = new HashMap(this.properties)
        properties.remove('errorHandler')
        JobExec ce = new JobExec(properties)
        return ce
    }
    /**
    * Return canonical map representation
     */
    public Map toMap(){
        final Map map = [jobref: [group: jobGroup ? jobGroup : '', name: jobName]]
        if(jobProject){
            map.project = jobProject
        }
        if(argString){
            map.jobref.args=argString
        }
        if(nodeStep){
            map.jobref.nodeStep="true"
        }
        if (errorHandler) {
            map.errorhandler = errorHandler.toMap()
        } else if (keepgoingOnSuccess) {
            map.keepgoingOnSuccess = keepgoingOnSuccess
        }
        if (description) {
            map.description = description
        }
        if(nodeFilter){
            map.jobref.nodefilters=[filter:nodeFilter]
            def dispatch=[:]
            if(null!=nodeThreadcount && nodeThreadcount>0){
                dispatch.threadcount=nodeThreadcount
            }
            if(null!=nodeKeepgoing){
                dispatch.keepgoing=!!nodeKeepgoing
            }
            if(nodeRankAttribute){
                dispatch.rankAttribute=nodeRankAttribute
            }
            if(null!=nodeRankOrderAscending){
                dispatch.rankOrder=nodeRankOrderAscending?'ascending':'descending'
            }
            if(null!=nodeIntersect){
                dispatch.nodeIntersect=nodeIntersect
            }
            if(dispatch){
                map.jobref.nodefilters.dispatch=dispatch
            }
        } else if (null != nodeIntersect) {
            map.jobref.nodefilters = [dispatch: [nodeIntersect: nodeIntersect]]
        }
        return map
    }

    static JobExec jobExecFromMap(Map map){
        JobExec exec = new JobExec()
        exec.jobGroup=map.jobref.group
        exec.jobName=map.jobref.name
        if(map.jobref.jobProject){
            exec.jobProject = map.jobref.jobProject
        }
        if(map.jobref.args){
            exec.argString=map.jobref.args
        }
        if(map.jobref.nodeStep in ['true',true]){
            exec.nodeStep=true
        }else{
            exec.nodeStep=false
        }
        exec.keepgoingOnSuccess = !!map.keepgoingOnSuccess
        exec.description=map.description?.toString()
        if(map.jobref.nodefilters){
            exec.nodeFilter=map.jobref.nodefilters.filter?.toString()
            if(exec.nodeFilter){
                def dispatch = map.jobref.nodefilters.dispatch
                if(dispatch?.threadcount){
                    if(dispatch.threadcount instanceof Integer){
                        exec.nodeThreadcount= dispatch.threadcount ?: 1
                    }else{
                        exec.nodeThreadcount = Integer.parseInt(dispatch.threadcount.toString()) ?: 1
                    }
                }
                if(null!=dispatch?.keepgoing){
                    if (dispatch.keepgoing in ['true', true]) {
                        exec.nodeKeepgoing=true
                    }else{
                        exec.nodeKeepgoing=false
                    }
                }
                if (null != dispatch?.rankOrder) {
                    exec.nodeRankOrderAscending = (dispatch.rankOrder == 'ascending')
                }

                exec.nodeRankAttribute= dispatch?.rankAttribute
            }
            if(map.jobref.nodefilters.dispatch && null!=map.jobref.nodefilters.dispatch?.nodeIntersect){
                if (map.jobref.nodefilters.dispatch.nodeIntersect in ['true', true]) {
                    exec.nodeIntersect=true
                }else{
                    exec.nodeIntersect=false
                }
            }
        }
        //nb: error handler is created inside Workflow.fromMap
        return exec
    }
}
