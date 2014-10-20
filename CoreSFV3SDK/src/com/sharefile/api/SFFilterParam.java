package com.sharefile.api;


import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.utils.Utils;

import java.util.ArrayList;

public class SFFilterParam
{
    private final ArrayList<String> mGenericFilterValues = new ArrayList<String>();
    private final ArrayList<SFV3ElementType> mAndType = new ArrayList<SFV3ElementType>();
    private final ArrayList<SFV3ElementType> mORType = new ArrayList<SFV3ElementType>();
    private final String OR = " or ";
    private final String AND = " and ";

    SFFilterParam and(SFV3ElementType type)
    {
        mAndType.add(type);
        return this;
    }

    SFFilterParam or(SFV3ElementType type)
    {
        mORType.add(type);
        return this;
    }

    SFFilterParam is(SFV3ElementType type)
    {
        mORType.add(type);//Or this!!
        return this;
    }

    SFFilterParam filter(String value)
    {
        mGenericFilterValues.add(value);
        return this;
    }

    private <T> void addAllFiltersWithOperator(String operator,ArrayList<T> filterValues,ArrayList<String> dest, boolean first)
    {
        if(!Utils.isEmpty(filterValues))
        {
            for(T str: filterValues)
            {
                if (!first)
                {
                    dest.add(operator);
                }
                else
                {
                    first = false;
                }

                String type = null;

                if(str instanceof SFV3ElementType)
                {
                    type = ((SFV3ElementType)str).type();
                }
                else
                {
                    type = str.toString();
                }

                dest.add("'"+type+"'");
            }
        }
    }

    public String get()
    {
        ArrayList<String> merged = new ArrayList<String>();

        addAllFiltersWithOperator(OR, mORType, merged, true);
        addAllFiltersWithOperator(AND, mAndType, merged, merged.size()>0);

        if(Utils.isEmpty(merged))
        {
               return  null;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("isof(");
        for(String str: merged)
        {
            sb.append(str);
        }
        sb.append(")");

        return sb.toString();
    }
}