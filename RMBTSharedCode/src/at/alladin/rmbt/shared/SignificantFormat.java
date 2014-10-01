/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.shared;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class SignificantFormat extends NumberFormat
{
    private static final long serialVersionUID = 1L;
    
    private final NumberFormat format;
    private final MathContext mathContext;
    
    public SignificantFormat(final int significantPlaces)
    {
        this(significantPlaces, Locale.getDefault());
    }
    
    public SignificantFormat(final int significantPlaces, final Locale locale)
    {
        format = NumberFormat.getNumberInstance(locale);
        mathContext = new MathContext(significantPlaces, RoundingMode.HALF_UP);
    }
    
    @Override
    public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return format.format(new BigDecimal(number, mathContext), toAppendTo, pos);
    }
    
    @Override
    public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return format.format(new BigDecimal(number, mathContext), toAppendTo, pos);
    }
    
    @Override
    public Number parse(final String text, final ParsePosition pos)
    {
        return format.parse(text, pos);
    }
}
