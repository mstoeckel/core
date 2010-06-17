package org.jboss.weld.examples.pastecode.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class TruncatingConverter implements Converter
{

   // The max length of the snippet we show
   private static int TRIMMED_TEXT_LEN = 100;
   
   public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
   {
      return arg2;
   }

   public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
   {
      String text = arg2.toString();
      return text.length() < TRIMMED_TEXT_LEN ? text : text.substring(0, TRIMMED_TEXT_LEN) + "\n ...";
   }

}
