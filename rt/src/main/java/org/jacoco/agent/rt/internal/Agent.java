/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.internal.output.IAgentOutput;
import org.jacoco.agent.rt.internal.output.NoneOutput;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RuntimeData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * The agent manages the life cycle of JaCoCo runtime.
 */
public class Agent implements IAgent {

	private static Agent singleton;

	/**
	 * Returns a global instance which is already started. If the method is
	 * called the first time the instance is created with the given options.
	 *
	 *            options to configure the instance
	 * @return global instance
	 * @throws Exception
	 *             in case something cannot be initialized
	 */
	public static synchronized Agent getInstance()
			throws Exception {
		if (singleton == null) {
			final Agent agent = new Agent(IExceptionLogger.SYSTEM_ERR);
			agent.startup();
//			Runtime.getRuntime().addShutdownHook(new Thread() {
//				@Override
//				public void run() {
//					agent.shutdown();
//				}
//			});
			singleton = agent;
		}
		return singleton;
	}

	private final IExceptionLogger logger;

	private final RuntimeData data;

	private IAgentOutput output;

	private Callable<Void> jmxRegistration;

	/**
	 * Creates a new agent with the given agent options.
	 *
	 * @param logger
	 *            logger used by this agent
	 */
	Agent(final IExceptionLogger logger) {
		this.logger = logger;
		this.data = new RuntimeData();
	}

	/**
	 * Returns the runtime data object created by this agent
	 *
	 * @return runtime data for this agent instance
	 */
	public RuntimeData getData() {
		return data;
	}

	/**
	 * Initializes this agent.
	 *
	 * @throws Exception
	 *             in case something cannot be initialized
	 */
	public void startup() throws Exception {
		try {
			String sessionId = createSessionId();
			data.setSessionId(sessionId);
			output = createAgentOutput();
			output.startup(data);
		} catch (final Exception e) {
			logger.logExeption(e);
			throw e;
		}
	}

	/**
	 * Shutdown the agent again.
	 */
	public void shutdown() {
		try {
			output.writeExecutionData(false);
			output.shutdown();
			if (jmxRegistration != null) {
				jmxRegistration.call();
			}
		} catch (final Exception e) {
			logger.logExeption(e);
		}
	}

	/**
	 * Create output implementation as given by the agent options.
	 *
	 * @return configured controller implementation
	 */
	IAgentOutput createAgentOutput() {
		return new NoneOutput();
	}

	private String createSessionId() {
		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (final Exception e) {
			// Also catch platform specific exceptions (like on Android) to
			// avoid bailing out here
			host = "unknownhost";
		}
		return host + "-" + createRandomId();
	}

	private static final Random RANDOM = new Random();
	public static String createRandomId() {
		return Integer.toHexString(RANDOM.nextInt());
	}

	// === IAgent Implementation ===

	public String getVersion() {
		return JaCoCo.VERSION;
	}

	public String getSessionId() {
		return data.getSessionId();
	}

	public void setSessionId(final String id) {
		data.setSessionId(id);
	}

	public void reset() {
		data.reset();
	}

	public byte[] getExecutionData(final boolean reset) {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			final ExecutionDataWriter writer = new ExecutionDataWriter(buffer);
			data.collect(writer, writer, reset);
		} catch (final IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new AssertionError(e);
		}
		return buffer.toByteArray();
	}

	public void dump(final boolean reset) throws IOException {
		output.writeExecutionData(reset);
	}

}
