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
package com.liferay.arquillian.test;

import com.liferay.arquillian.processor.BndApplicationArchiveProcessor;
import com.liferay.arquillian.test.extras.a.A;
import com.liferay.arquillian.test.extras.b.B;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

/**
 * @author Carlos Sierra Andrés
 */
public class BndApplicationArchiveProcessorTest {

	public static final String ORIGINAL_BND_FILE = "##original_bnd_file##";

	@Test
	public void testNoManifestAndOriginalBnd() throws IOException, BundleException {
		BndApplicationArchiveProcessor bndApplicationArchiveProcessor = new BndApplicationArchiveProcessor();

		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

		javaArchive.add(new StringAsset(
			"Bundle-Name: A Test\n" +
			"Include-Resource: classes\n" +
			"Private-Package: *\n"
		), ORIGINAL_BND_FILE);

		bndApplicationArchiveProcessor.process(javaArchive, new TestClass(ATest.class));

		Manifest manifest = getManifest(javaArchive);

		List<String> importPackage = getHeaderAsList(manifest, "Import-Package");

		Assert.assertTrue(importPackage.contains(A.class.getPackage().getName()));
		Assert.assertTrue(importPackage.contains(B.class.getPackage().getName()));

		List<String> exportPackage = getHeaderAsList(manifest, "Export-Package");

		Assert.assertTrue(exportPackage.contains(ATest.class.getPackage().getName()));
	}

	@Test
	public void testEmptyFile() throws IOException, BundleException {
		BndApplicationArchiveProcessor bndApplicationArchiveProcessor = new BndApplicationArchiveProcessor();

		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

		bndApplicationArchiveProcessor.process(javaArchive, new TestClass(ATest.class));

		Manifest manifest = getManifest(javaArchive);

		List<String> importPackage = getHeaderAsList(manifest, "Import-Package");

		Assert.assertTrue(importPackage.contains(A.class.getPackage().getName()));
		Assert.assertTrue(importPackage.contains(B.class.getPackage().getName()));

		List<String> exportPackage = getHeaderAsList(manifest, "Export-Package");

		Assert.assertTrue(exportPackage.contains(ATest.class.getPackage().getName()));
	}

	@Test
	public void testMergeExport() throws IOException, BundleException {
		BndApplicationArchiveProcessor bndApplicationArchiveProcessor = new BndApplicationArchiveProcessor();

		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

		javaArchive.addClass(A.class);

		javaArchive.setManifest(new StringAsset(
			"Bundle-Name: A Test\n" +
			"Include-Resource: classes\n" +
			"Export-Package: com.liferay.arquillian.test.extras.a\n"
		));

		bndApplicationArchiveProcessor.process(javaArchive, new TestClass(ATest.class));

		Manifest manifest = getManifest(javaArchive);

		List<String> importPackage = getHeaderAsList(manifest, "Import-Package");

		Assert.assertTrue(importPackage.contains(B.class.getPackage().getName()));

		List<String> exportPackage = getHeaderAsList(manifest, "Export-Package");

		System.out.println(exportPackage);

		Assert.assertTrue(exportPackage.contains(ATest.class.getPackage().getName()));
		Assert.assertTrue(exportPackage.contains(A.class.getPackage().getName()));
	}

	@Test
	public void testImportPackagesIsNotOverriden() throws IOException, BundleException {
		BndApplicationArchiveProcessor bndApplicationArchiveProcessor = new BndApplicationArchiveProcessor();

		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

		javaArchive.add(new StringAsset(
			"Bundle-Name: A Test\n" +
			"Include-Resource: classes\n" +
			"Import-Package: com.liferay.arquillian.test.extras.a\n"
		), ORIGINAL_BND_FILE);

		bndApplicationArchiveProcessor.process(javaArchive, new TestClass(ATest.class));

		Manifest manifest = getManifest(javaArchive);

		List<String> importPackage = getHeaderAsList(manifest, "Import-Package");

		Assert.assertFalse(importPackage.contains(B.class.getPackage().getName()));
	}

	@Test
	public void testOriginalBndFileIsRemoved() {
		BndApplicationArchiveProcessor bndApplicationArchiveProcessor = new BndApplicationArchiveProcessor();

		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class);

		javaArchive.add(new StringAsset(
			"Bundle-Name: A Test\n" +
				"Include-Resource: classes\n" +
				"Import-Package: com.liferay.arquillian.test.extras.a\n"
		), ORIGINAL_BND_FILE);

		Assert.assertNotNull(javaArchive.get(ORIGINAL_BND_FILE));

		bndApplicationArchiveProcessor.process(javaArchive, new TestClass(ATest.class));

		Assert.assertNull(javaArchive.get(ORIGINAL_BND_FILE));
	}

	private Manifest getManifest(JavaArchive javaArchive) throws IOException {
		return new Manifest(javaArchive.get("META-INF/MANIFEST.MF").getAsset().openStream());
	}

	private List<String> getHeaderAsList(Manifest manifest, String header) throws BundleException {
		String importPackageString = manifest.getMainAttributes().getValue(header);

		ManifestElement[] manifestElements = ManifestElement.parseHeader(header, importPackageString);

		ArrayList<String> values = new ArrayList<>(manifestElements.length);

		for (ManifestElement manifestElement : manifestElements) {
			values.add(manifestElement.getValue());
		}

		return values;
	}
}
