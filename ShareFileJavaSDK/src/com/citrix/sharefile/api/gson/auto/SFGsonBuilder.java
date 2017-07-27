package com.citrix.sharefile.api.gson.auto;

import com.citrix.sharefile.api.enumerations.SFSafeEnum;
import com.citrix.sharefile.api.enumerations.SFSafeEnumFlags;
import com.citrix.sharefile.api.models.SFItem;
import com.citrix.sharefile.api.models.SFMetadata;
import com.citrix.sharefile.api.models.SFODataFeed;
import com.citrix.sharefile.api.models.SFPrincipal;
import com.citrix.sharefile.api.utils.SFDateFormat;
import com.citrix.sharefile.api.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

/**
   Builds the Gson object required to parse strings to Java Objects.
   It will by default add GsonRouter which allows us to figure out the ShareFile Models class from the odatatype
   field in the json string so that we can map generic class variables like SFItem to Specific classes
   like SFFile,SFolder etc.
*/
public class SFGsonBuilder
{
    private static final HashMap<Type,Object> mRegisteredForInternalRouting = new HashMap<Type,Object>();
    private static final HashMap<Type, Gson> gsonObjectsToAvoidCustomParsing = new HashMap<>();
    private static Gson customParseAll;

    private static Gson getCachedGson(Type classToIgnoreFromSFGsonRouting, SFDefaultGsonParser gsonParser){
        if(classToIgnoreFromSFGsonRouting == null){
            if(customParseAll == null){
                customParseAll = buildGson(null,gsonParser);
            }
            return customParseAll;
        }

        return gsonObjectsToAvoidCustomParsing.get(classToIgnoreFromSFGsonRouting);
    }

    private static Gson buildGson(Type classToIgnoreFromSFGsonRouting, SFDefaultGsonParser gsonParser){

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.disableHtmlEscaping();
        registerSFSpecificGsonAdapters(classToIgnoreFromSFGsonRouting,gsonBuilder,gsonParser);
        Gson ret = gsonBuilder.setDateFormat("yyyy-MM-dd").create();

        //Cache the Gson object which ignores custom parsing for specific classes so that it can be re-used.
        if(classToIgnoreFromSFGsonRouting !=null){
            gsonObjectsToAvoidCustomParsing.put(classToIgnoreFromSFGsonRouting,ret);
        }

        return ret;
    }

    public static Gson build(Type classToIgnoreFromSFGsonRouting, SFDefaultGsonParser gsonParser)
    {
        Gson ret = getCachedGson(classToIgnoreFromSFGsonRouting,gsonParser);
        if(ret!=null){
            return ret;
        }

        return buildGson(classToIgnoreFromSFGsonRouting,gsonParser);
    }

    private static void registerInternalRouter(Type type,Object object, Type classToIgnoreFromSFGsonRouting, GsonBuilder gsonBuilder)
    {
        mRegisteredForInternalRouting.put(type,object);

        //Avoid registering the type adapter
        if(type == classToIgnoreFromSFGsonRouting){
            return;
        }

        gsonBuilder.registerTypeAdapter(type, object);
    }

    public static boolean isRegisteredForInternalGsonRouting(Type type){
        return  (mRegisteredForInternalRouting.get(type)!=null);
    }

    private static void registerSFSpecificGsonAdapters(Type classToIgnoreFromSFGsonRouting, GsonBuilder gsonBuilder, SFDefaultGsonParser gsonParser)
    {
        registerInternalRouter(SFPrincipal.class, new SFGsonRouter(gsonParser),classToIgnoreFromSFGsonRouting,gsonBuilder);
        registerInternalRouter(SFItem.class, new SFGsonRouter(gsonParser),classToIgnoreFromSFGsonRouting,gsonBuilder);
        registerInternalRouter(SFODataFeed.class, new SFGsonRouter(gsonParser),classToIgnoreFromSFGsonRouting,gsonBuilder);
        registerInternalRouter(SFSafeEnum.class, new SFCustomSafeEnumParser(),classToIgnoreFromSFGsonRouting,gsonBuilder);
        registerInternalRouter(SFSafeEnumFlags.class, new SFCustomSafeEnumFlagsParser(),classToIgnoreFromSFGsonRouting,gsonBuilder);
        registerInternalRouter(SFMetadata.class, new SFGsonRouter(gsonParser),classToIgnoreFromSFGsonRouting,gsonBuilder);

        registerInternalRouter(Date.class, new JsonDeserializer<Date>()
        {
            @Override
            public Date deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException
            {
                return SFDateFormat.parse(arg0.getAsString());
            }
        },classToIgnoreFromSFGsonRouting,gsonBuilder);

        registerInternalRouter(URI.class, new JsonDeserializer<URI>()
        {
            @Override
            public URI deserialize(JsonElement arg0, Type arg1,JsonDeserializationContext arg2) throws JsonParseException
            {
                try
                {
                    return Utils.getURIFromString(arg0.getAsString());
                }
                catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e)
                {
                    throw new JsonParseException(e);
                }
            }
        },classToIgnoreFromSFGsonRouting,gsonBuilder);
    }
}
