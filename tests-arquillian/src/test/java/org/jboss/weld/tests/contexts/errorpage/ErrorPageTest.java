/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.tests.contexts.errorpage;

import java.util.HashSet;
import java.util.Set;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * <p>This test was mostly developed to test the scenario related to WELD-29.  Essentially
 * a JSF action throws an exception, and the error page is then rendered during which
 * all relevant scopes for CDI are tested.</p>
 *
 * @author David Allen
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ErrorPageTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
               .addClasses(Storm.class, Rain.class)
               .addAsWebInfResource(ErrorPageTest.class.getPackage(), "web.xml", "web.xml")
               .addAsWebInfResource(ErrorPageTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
               .addAsWebResource(ErrorPageTest.class.getPackage(), "error.jsf", "error.jspx")
               .addAsWebResource(ErrorPageTest.class.getPackage(), "storm.jsf", "storm.jspx")
               .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
   }

   /*
    * description = "WELD-29"
    */
   @Category(Broken.class)
   @Test
   public void testActionMethodExceptionDoesNotDestroyContext() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);

      HtmlPage page = client.getPage(getPath("/storm.jsf"));
      HtmlSubmitInput disasterButton = getFirstMatchingElement(page, HtmlSubmitInput.class, "disasterButton");
      HtmlTextInput strength = getFirstMatchingElement(page, HtmlTextInput.class, "stormStrength");
      strength.setValueAttribute("10");
      page = disasterButton.click();
      Assert.assertEquals("Application Error", page.getTitleText());

      HtmlDivision conversationValue = getFirstMatchingElement(page, HtmlDivision.class, "conversation");
      Assert.assertEquals("10", conversationValue.asText());

      HtmlDivision requestValue = getFirstMatchingElement(page, HtmlDivision.class, "request");
      Assert.assertEquals("medium", requestValue.asText());
   }

   protected String getPath(String page)
   {
      // TODO: this should be moved out and be handled by Arquillian
      return "http://localhost:8080/test/" + page;
   }

   protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass)
   {
     Set<T> result = new HashSet<T>();

     for (HtmlElement element : rootElement.getAllHtmlChildElements())
     {
        result.addAll(getElements(element, elementClass));
     }

     if (elementClass.isInstance(rootElement))
     {
        result.add(elementClass.cast(rootElement));
     }
     return result;

   }

   protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id)
   {

     Set<T> inputs = getElements(page.getBody(), elementClass);
     for (T input : inputs)
     {
         if (input.getId().contains(id))
         {
            return input;
         }
     }
     return null;
   }
}