//
// ****************************************************************************
// * Copyright (C) 2016, International Business Machines Corporation          *
// * All rights reserved.                                                     *
// ****************************************************************************
//

package com.ibm.streamsx.monitoring.jmx.internal;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Set;

import javax.management.JMX;
import javax.management.ObjectName;

import com.ibm.streams.management.Metric;
import com.ibm.streams.management.ObjectNameBuilder;
import com.ibm.streams.management.job.PeOutputPortMXBean;
import com.ibm.streamsx.monitoring.jmx.OperatorConfiguration;

/**
 * 
 */
public class PeOutputPortHandler extends MetricOwningHandler implements Closeable {

	/**
	 * Logger for tracing.
	 */
	private static Logger _trace = Logger.getLogger(PeOutputPortHandler.class.getName());

	private String _domainId = null;

	private String _instanceId = null;
	
	private BigInteger _jobId = null;
	
	private String _jobName = null;
	
	private BigInteger _peId = null;
	
	private Integer _portIndex = null;
	
	private PeOutputPortMXBean _port = null;

	public PeOutputPortHandler(OperatorConfiguration operatorConfiguration, String domainId, String instanceId, BigInteger jobId, String jobName, BigInteger peId, Integer portIndex) {

		super(MetricsRegistrationMode.DynamicMetricsRegistration);

		// Determine the trace level status once per function.
		boolean isDebugEnabled = _trace.isDebugEnabled();

		// Store parameters for later use.
		_operatorConfiguration = operatorConfiguration;
		_domainId = domainId;
		_instanceId = instanceId;
		_jobId = jobId;
		_jobName = jobName;
		_peId = peId;
		_portIndex = portIndex;

		if (isDebugEnabled) {
			_trace.debug("--> OutputPortHandler(domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "]:" + _jobName + ", peId=" + _peId + ", port=" + _portIndex + ")");
		}
		
		ObjectName objName = ObjectNameBuilder.peOutputPort(_domainId, _instanceId, _peId, _portIndex);
		_port = JMX.newMXBeanProxy(_operatorConfiguration.get_mbeanServerConnection(), objName, PeOutputPortMXBean.class, true);
		
		/*
		 * Register output port metrics that match the specified filter criteria.
		 */
		registerMetrics();
		
		if (isDebugEnabled) {
			_trace.debug("<-- OutputPortHandler(domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "]:" + _jobName + ", peId=" + _peId + ", port=" + _portIndex + ")");
		}
	}

	@Override
	protected boolean isRelevantMetric(String metricName) {
		boolean isRelevant = _operatorConfiguration.get_filters().matchesPeOutputPortMetricName(_domainId, _instanceId, _jobName, _peId, _portIndex, metricName);
		if (_trace.isInfoEnabled()) {
			if (isRelevant) {
				_trace.info("The following output port metric meets the filter criteria and is therefore, monitored: domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "][" + _jobName + "], peId=" + _peId + ", port=" + _portIndex + ", metric=" + metricName);
			}
			else { 
				_trace.info("The following output port metric does not meet the filter criteria and is therefore, not monitored: domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "][" + _jobName + "], peId=" + _peId + ", port=" + _portIndex + ", metric=" + metricName);
			}
		}
		return isRelevant;
	}

	@Override
	protected Set<Metric> retrieveMetrics() {
		Set<Metric> metrics = _port.retrieveMetrics();
		return metrics;
	}

	/**
	 * Iterate all jobs to capture the job metrics.
	 * @throws Exception 
	 */
	public void captureMetrics() throws Exception {

		// Determine the trace level status once per function.
		boolean isDebugEnabled = _trace.isDebugEnabled();

		if (isDebugEnabled) {
			_trace.debug("--> captureMetrics(domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "]:" + _jobName + ", peId=" + _peId + ", port=" + _portIndex + ")");
		}
		MetricsTupleContainer tc = _operatorConfiguration.get_tupleContainerMetricsSource();
		tc.setOrigin("PeOutputPort");
		tc.setPortIndex(_portIndex);
		captureAndSubmitChangedMetrics();

		if (isDebugEnabled) {
			_trace.debug("<-- captureMetrics(domain=" + _domainId + ", instance=" + _instanceId + ", job=[" + _jobId + "]:" + _jobName + ", peId=" + _peId + ", port=" + _portIndex + ")");
		}
	}

}
