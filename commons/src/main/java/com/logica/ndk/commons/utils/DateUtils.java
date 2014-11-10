package com.logica.ndk.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public abstract class DateUtils {

  public static final XMLGregorianCalendar toXmlDateTime(final Date date) {
    if (date == null) {
      return null;
    }

    final GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }
    catch (final DatatypeConfigurationException e) {
      throw new RuntimeException("Implementation is not available or cannot be instantiated", e);
    }
  }

  public static final XMLGregorianCalendar toXmlDateTime(final String dateString) throws ParseException {
    if (dateString == null) {
      return null;
    }

    return toXmlDateTime(toDate(dateString));
  }

  public static final Date toDate(final XMLGregorianCalendar calendar) {
    if (calendar == null) {
      return null;
    }

    return calendar.toGregorianCalendar().getTime();
  }

  public static final Date toDate(final String dateString) throws ParseException {
    if (dateString == null) {
      return null;
    }
    // 2012-02-15T12:20:33.935+01:00
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    return dateFormat.parse(dateString);
  }

}
