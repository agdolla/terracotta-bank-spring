/*
 * Copyright 2015-2018 Josh Cummings
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
package com.joshcummings.codeplay.terracotta;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.joshcummings.codeplay.terracotta.testng.XssCheatSheet;

public class RegisterFunctionalTest extends AbstractEmbeddedTomcatSeleniumTest {

	@AfterClass(alwaysRun=true)
	public void doLogout() {
		logout();
	}

	@Test(groups="web")
	public void testRegisterForXSS() {
		for (String template : new XssCheatSheet()) {
			goToPage("/");
			
			try {
				String username = String.format(template, "registerUsername");
				String password = String.format(template, "registerPassword");
				String name = String.format(template, "registerName");
				String email = String.format(template, "registerEmail");

				driver.findElement(By.name("registerUsername")).sendKeys(username);
				driver.findElement(By.name("registerPassword")).sendKeys(password);
				driver.findElement(By.name("registerName")).sendKeys(name);
				driver.findElement(By.name("registerEmail")).sendKeys(email);
				driver.findElement(By.name("register")).submit();

				Alert alert = switchToAlertEventually(driver, 2000);
				Assert.fail(getTextThenDismiss(alert) + " using " + template);
			} catch (NoAlertPresentException e) {
				// awesome!
			}
			
			logout();
		}

	}

	@Test(groups="password")
	public void testRegisterWithShortPassword() {
		String response = attemptRegistration("username", "1P@ss!");
		Assert.assertTrue(response.contains("doesn't meet our security guidelines"));
	}

	@Test(groups="password")
	public void testRegisterWithLongPassword() {
		String response = attemptRegistration("username", "longhorn-pacifiers-running-witherspoon-distilleries");
		Assert.assertTrue(response.contains("Welcome, Partridge"));
	}

	@Test(groups="password")
	public void testRegisterWithPasswordContainingSpaces() {
		String response = attemptRegistration("username", "longhorn pacifiers running witherspoon distilleries");
		Assert.assertTrue(response.contains("Welcome, Partridge"));
	}

	@Test(groups="password")
	public void testRegisterWithPasswordUsingDictionaryWord() {
		String response = attemptRegistration("username", "longhorn");
		Assert.assertTrue(response.contains("doesn't meet our security guidelines"));
	}

	@Test(groups="password")
	public void testRegisterWithPasswordUsingLeetifiedDictionaryWord() {
		String response = attemptRegistration("username", "L0ngh0rn!");
		Assert.assertTrue(response.contains("doesn't meet our security guidelines"));
	}

	@Test(groups="password")
	public void testRegisterWithPatternedPassword() {
		String response = attemptRegistration("username", "1357924680Abc!");
		Assert.assertTrue(response.contains("doesn't meet our security guidelines"));
	}

	@Test(groups="password")
	public void testRegisterWithCommonPassword() {
		String response = attemptRegistration("username", "P@ssw0rd!");
		Assert.assertTrue(response.contains("doesn't meet our security guidelines"));
	}

	private String attemptRegistration(String username, String password) {
		return
				http.postForContent(RequestBuilder.post("/register")
						.addParameter("registerUsername", username)
						.addParameter("registerPassword", password)
						.addParameter("registerName", "Partridge Peartree")
						.addParameter("registerEmail", "partridge@peartree.com"));
	}
}
