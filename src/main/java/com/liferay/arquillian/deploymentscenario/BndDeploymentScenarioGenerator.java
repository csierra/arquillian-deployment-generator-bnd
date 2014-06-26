/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.arquillian.deploymentscenario;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.osgi.api.BndProjectBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Sierra Andrés
 */
public class BndDeploymentScenarioGenerator implements DeploymentScenarioGenerator {

	@Override
	public List<DeploymentDescription> generate(TestClass testClass) {
		ArrayList<DeploymentDescription> deployments = new ArrayList<>();

		try {
			JavaArchive javaArchive = ShrinkWrap.create(BndProjectBuilder.class).setBndFile(new File("bnd.bnd")).as(JavaArchive.class);

			DeploymentDescription deploymentDescription = new DeploymentDescription(javaArchive.getName(), javaArchive);

			deploymentDescription.shouldBeTestable(true);

			deployments.add(deploymentDescription);

			return deployments;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
