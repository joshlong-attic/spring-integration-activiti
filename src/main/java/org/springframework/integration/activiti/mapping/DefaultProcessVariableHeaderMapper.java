/*Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package org.springframework.integration.activiti.mapping;


import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.activiti.ActivitiConstants;
import org.springframework.integration.activiti.ProcessSupport;
import org.springframework.integration.activiti.util.ActivityExecutionFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * A {@link org.springframework.integration.mapping.HeaderMapper} implementation for mapping
 * to and from a Map which will ultimately be add via {@link org.activiti.engine.RuntimeService#setVariable(String, String, Object)}.
 * <p/>
 * The {@link #processVariableToHeaderNames} and {@link #headerToProcessVariableNames} may be configured.
 * <p/>
 * They accept exact name Strings or simple patterns (e.g. "start*", "*end", or "*").
 * <p/>
 * By default all headers in {@link org.springframework.integration.activiti.ActivitiConstants} will be accepted.
 * <p/>
 * Any outbound header that should be mapped must be configured explicitly. Note that the outbound mapping only writes
 * String header values into attributes on the header. For anything more advanced, one should implement the HeaderMapper interface directly.
 *
 * @author Josh Long
 * @since 5.1
 */
public class DefaultProcessVariableHeaderMapper implements ProcessVariableHeaderMapper, InitializingBean {

    /**
     * cached length of the header prefix
     */
    private int wellKnownHeaderPrefixLength;

    /**
     * all headers that we want to forward as process variables. None, by default, as headers may be rich objects where as process variables <em>should</em> be lightweight (primitives, for example)
     */
    private String[] headerToProcessVariableNames = new String[0];

    /**
     * all process variables that should be exposed as headers
     */
    private String[] processVariableToHeaderNames = new String[]{"*"};

    private boolean shouldPrefixProcessVariables = false;

    private String prefix = ActivitiConstants.WELL_KNOWN_SPRING_INTEGRATION_HEADER_PREFIX;

    private Log log = LogFactory.getLog(getClass());

    /**
     * by default, we'll also correctly forward keys starting with {@link ActivitiConstants#WELL_KNOWN_SPRING_INTEGRATION_HEADER_PREFIX}
     */
    private boolean includeHeadersWithWellKnownPrefix = true;

    /**
     * this shall be a thread-safe proxy so that this class may be configured once and then reused in a thread-safe way through subsequent accesses
     * and also take advantage of unit testing.
     */
    private volatile ActivityExecution activitiExecution;

    /**
     * headers that are automatically propagated, if so configured
     */
    private Set<String> wellKnownActivitiHeaders =
            new HashSet<String>(Arrays.asList(ActivitiConstants.WELL_KNOWN_ACTIVITY_ID_HEADER_KEY,
                                                     ActivitiConstants.WELL_KNOWN_EXECUTION_ID_HEADER_KEY,
                                                     ActivitiConstants.WELL_KNOWN_PROCESS_INSTANCE_ID_HEADER_KEY,
                                                     ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_ID_HEADER_KEY,
                                                     ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY));

    private boolean matchesAny(String[] patterns, String candidate) {
        for (String pattern : patterns) {
            if (PatternMatchUtils.simpleMatch(pattern, candidate)) {
                return true;
            }
        }
        return false;
    }

    public DefaultProcessVariableHeaderMapper(ActivityExecution e) {
        setCurrentActivityExecution(e);
    }

    public DefaultProcessVariableHeaderMapper() {
        try {
            ActivityExecutionFactoryBean activityExecutionFactoryBean = new ActivityExecutionFactoryBean();
            activityExecutionFactoryBean.afterPropertiesSet();
            setCurrentActivityExecution(activityExecutionFactoryBean.getObject());

        } catch (Throwable throwable) {

            if (log.isErrorEnabled()) {
                log.error("Exception occurred when trying to invoke " + ActivityExecutionFactoryBean.class.getName() + "#getObject()");
            }

            throw new RuntimeException(throwable);
        }
    }

    /**
     * whether or not we should include fields that begin with the {@link #prefix}
     *
     * @param includeHeadersWithWellKnownPrefix
     *         should we honor well known prefixes found on incoming Message headers?
     */
    public void setIncludeHeadersWithWellKnownPrefix(boolean includeHeadersWithWellKnownPrefix) {
        this.includeHeadersWithWellKnownPrefix = includeHeadersWithWellKnownPrefix;
    }

    public void setCurrentActivityExecution(ActivityExecution ae) {
        this.activitiExecution = ae;
    }

    public void fromHeaders(MessageHeaders headers, Map<String, Object> target) {
        Assert.notNull(target, "the target can't be null");

        Map<String, Object> procVars = new HashMap<String, Object>();

        for (String messageHeaderKey : headers.keySet()) {
            if (shouldMapHeaderToProcessVariable(messageHeaderKey)) {
                String pvName = messageHeaderKey.startsWith(prefix)
                                        ? messageHeaderKey.substring(wellKnownHeaderPrefixLength)
                                        : messageHeaderKey;
                procVars.put(pvName, headers.get(messageHeaderKey));

                if (log.isDebugEnabled()) {
                    log.debug(String.format("mapping header '%s' to process variable '%s'", messageHeaderKey, pvName));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("NOT mapping header '%s' to process variable", messageHeaderKey));
                }
            }
        }

        for (String k : procVars.keySet()) {
            target.put(k, procVars.get(k));
        }
    }

    private boolean shouldMapHeaderToProcessVariable(String headerName) {
        Assert.isTrue(StringUtils.hasText(headerName), "the header must not be empty");

        // first test. it might just be a direct match with something that has the prefix
        if (this.includeHeadersWithWellKnownPrefix && headerName.startsWith(prefix)) {
            return true;
        }

        if (this.wellKnownActivitiHeaders.contains(headerName)) {
            return true;
        }

        // if this didnt work, then we scan to see if the headers match a fuzzy algorithm
        return matchesAny(this.headerToProcessVariableNames, headerName);
    }

    private boolean shouldMapProcessVariableToHeader(String procVarName) {
        Assert.notNull(StringUtils.hasText(procVarName), "the process variable must not be null");

        return matchesAny(this.processVariableToHeaderNames, procVarName);
    }

    public void setShouldPrefixProcessVariables(boolean shouldPrefixProcessVariables) {
        this.shouldPrefixProcessVariables = shouldPrefixProcessVariables;
    }

    /**
     * the variables coming in from a given {@link ActivityExecution} will be mapped out as Spring Integration message headers.
     *
     * @param processVariables the processVariables
     * @return a map of headers to send with the Spring Integration message
     */
    public Map<String, ?> toHeaders(Map<String, Object> processVariables) {
        Map<String, Object> headers = new HashMap<String, Object>();

        for (String mhk : processVariables.keySet()) {
            if (shouldMapProcessVariableToHeader(mhk)) {
                String hKey = (this.shouldPrefixProcessVariables && StringUtils.hasText(this.prefix))
                                      ? this.prefix + mhk
                                      : mhk;

                if( log.isDebugEnabled())
                    log.debug(String.format( "mapping process variable '%s' to header '%s'", hKey,  (mhk)));

                headers.put(hKey, processVariables.get(mhk));
            } else {
                if(log.isDebugEnabled())
                log.debug(String.format( "NOT mapping process variable '%s' to header",   (mhk)));
            }
        }

        ProcessSupport.encodeCommonProcessDataIntoMessage(activitiExecution, headers);

        return headers;
    }

    public void afterPropertiesSet() throws Exception {
        // redundant but affords a chance to setup side effects of setting the prefix, like the wellKnownHeaderPrefixLength.
        setPrefix(this.prefix);
    }

    public void setHeaderToProcessVariableNames(String... h) {
        this.headerToProcessVariableNames = (null == h)
                                                    ? new String[0]
                                                    : h;
    }

    public void setProcessVariableToHeaderNames(String... ar) {
        this.processVariableToHeaderNames = (null == ar)
                                                    ? new String[0]
                                                    : ar;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.wellKnownHeaderPrefixLength = StringUtils.hasText(this.prefix)
                                                   ? this.prefix.length()
                                                   : 0;
    }
}
