/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.arquillian.processor;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.util.Properties;
import java.util.jar.Manifest;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * This processor analyzes the jar after adding the test to fix the imports
 * when needed.
 *
 * @author Carlos Sierra Andrés
 */
public class BndApplicationArchiveProcessor implements ApplicationArchiveProcessor {

	public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

	@Override
	public void process(Archive<?> applicationArchive, TestClass testClass) {

		JavaArchive javaArchive = applicationArchive.as(JavaArchive.class);

		javaArchive.addClass(testClass.getJavaClass());

		Analyzer analyzer = new Analyzer();

		try {
			Node originalBndNode = applicationArchive.delete("##original_bnd_file##");

			Properties properties = new Properties();

			if (originalBndNode != null) {
				properties.load(originalBndNode.getAsset().openStream());
			}

			analyzer.setProperties(properties);

			Node manifestNode = applicationArchive.get(MANIFEST_PATH);

			if (manifestNode != null) {
				analyzer.mergeManifest(new Manifest(manifestNode.getAsset().openStream()));
			}

			String exportPackage = analyzer.getProperty("Export-Package");

			if ((exportPackage == null) || exportPackage.isEmpty()) {
				exportPackage = testClass.getJavaClass().getPackage().getName();
			}
			else {
				exportPackage += "," + testClass.getJavaClass().getPackage().getName();
			}

			analyzer.setProperty("Export-Package", exportPackage);

			ZipExporter zipExporter = applicationArchive.as(ZipExporter.class);

			Jar jar = new Jar(applicationArchive.getName(), zipExporter.exportAsInputStream());

			analyzer.setJar(jar);

			Manifest manifest = analyzer.calcManifest();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			manifest.write(baos);

			ByteArrayAsset byteArrayAsset = new ByteArrayAsset(
				baos.toByteArray());

			replaceManifest(applicationArchive, byteArrayAsset);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		finally {
			analyzer.close();
		}
	}

	private void replaceManifest(
		Archive<?> archive, ByteArrayAsset byteArrayAsset) {

		archive.delete(MANIFEST_PATH);

		archive.add(byteArrayAsset, MANIFEST_PATH);
	}

}