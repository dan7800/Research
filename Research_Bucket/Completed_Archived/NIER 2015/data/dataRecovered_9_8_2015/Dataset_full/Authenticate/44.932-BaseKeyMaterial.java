/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.security;

import vu.globe.rts.java.*;
import vu.globe.rts.std.StdUtil;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.stdInf.*;
import vu.globe.util.debug.DebugOutput;

import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.*;            // ns.idl
import vu.globe.rts.runtime.cfg.idl.rtconfig.*;  // rtconfig.idl
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.rts.security.certs.RightsCertUtil;
import vu.gaia.rts.RTSException;

import java.io.IOException;
import java.util.Enumeration;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;


public class BaseKeyMaterial
{
    // If the following variable is set, the base credentials can be used.
    public boolean usable=false;
    
    // The name of the Globe runtime setting that determines the name
    // of the cacerts file.
    public static final String SSL_CACERTS_SETTING = "gids.schema.globeca";

    public static final String SECURITY_SETTINGS_PREFIX = "security.ssl";
    
    // The name of the Globe runtime setting that determines the name
    // of the keystore file.
    public static final String SSL_KEYSTORE_SETTING = 
                        SECURITY_SETTINGS_PREFIX+".KEYSTORE";
    
    // The name of the Globe runtime setting that determines the passphrase
    // of the keystore file.
    public static final String SSL_PASSPHRASE_SETTING =
                        SECURITY_SETTINGS_PREFIX+".KEYSTORE_PASSWD";
    
    // The name of the Globe runtime setting that determines which user
    // can do remote administration on the server. The value is the alias
    // of the user's certificate as stored in the cacerts file.
    public static final String SSL_REMOTEADMINALIAS_SETTING =
                        SECURITY_SETTINGS_PREFIX+".REMOTE_ADMIN_ALIAS";
    
    // The base cacerts (CA certificates) keystore.
    public KeyStore caKeyStore = null;
    
    // The base keystore itself.
    public KeyStore baseKeyStore = null;
    
    // The passphrase to check the integrity of the base keystore.
    public String basePassphrase=null;
    

    /**
     * Load the base credentials
     */
    public static BaseKeyMaterial loadBaseKeyMaterial()
    throws RTSException
    {
        String cacertsFile=null;
        String keyStoreFile=null;
        
        BaseKeyMaterial bkm = new BaseKeyMaterial();
        rtSettings settings = null;
        
        try
        {
            // bind to the settings object
            SOInf sett_soi = nameSpaceImp.getLNS().bind(context.NsRootCtx, nsConst.SETTINGS_NAME);
            settings = (rtSettings) sett_soi.swapInf(rtSettings.infid);
        }
        catch (Exception e)
        {
            throw new RTSException( e );
        }

        try
        {
            bkm.usable = true; //set to false when things fall apart.
            
            cacertsFile =  settings.getValue( SSL_CACERTS_SETTING );
            keyStoreFile = settings.getValue( SSL_KEYSTORE_SETTING );
            bkm.basePassphrase= settings.getValue( SSL_PASSPHRASE_SETTING );
            
           /*
            * The keystore and basePassphrase settings must both be (un)defined. It
            * is an error if one of them is not defined.
            */
            if (keyStoreFile != null)
            {
                if (bkm.basePassphrase == null)
                {
                    bkm.usable = false;
                    throw new RTSException( "BaseKeyMaterial: **** ERROR: " + SSL_PASSPHRASE_SETTING
                                                + " runtime setting not defined");
                }
            }
            else
            {
                bkm.usable = false;
                if (bkm.basePassphrase != null) // both undefined is OK, this isn't
                {
                    throw new RTSException( "BaseKeyMaterial: **** ERROR: " + SSL_KEYSTORE_SETTING
                    + " runtime setting not defined");
                }
            }
            
            if (!bkm.usable)
                return bkm;
            
            // Load keystores
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "rts: loading base keys from " + keyStoreFile );
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "rts: loading base CA certs from "+cacertsFile);
            bkm.caKeyStore = RightsCertUtil.loadKeyStore( cacertsFile, "" );
            bkm.baseKeyStore = RightsCertUtil.loadKeyStore(keyStoreFile, 
                                                           bkm.basePassphrase);
            
            return bkm;
        }
        catch (Exception e)
        {
            DebugOutput.print(DebugOutput.DBG_NORMAL,
                              "rts: BaseKeyMaterial: **** ERROR: " );
            DebugOutput.printException(DebugOutput.DBG_DEBUG, e );
            throw new RTSException( e );
        }
        finally
        {
            if (settings != null)
            {
                GInterface.RelInf(settings);
            }
        }
    }
    
    
    public PublicKey getPublicKey()
    throws RTSException
    {
        try
        {
            for (Enumeration e = baseKeyStore.aliases(); e.hasMoreElements(); )
            {
                String alias = (String)e.nextElement();
                if (baseKeyStore.isKeyEntry( alias ))
                {
                    return (PublicKey)baseKeyStore.getCertificate( alias ).getPublicKey();
                }
            }
        }
        catch( Exception x )
        {
            throw new RTSException( x );
        }
    
        throw new RTSException( this.getClass().getName() + " must contain public key!" );
    }
    
    
    
    /**
     * Set TLS passphrase if not set.
     */
    public static void setTLSPassphraseIfNotSet()
    throws IOException, RTSException
    {
        rtSettings settings = null;
        
        try
        {
            // bind to the settings object
            SOInf sett_soi = nameSpaceImp.getLNS().bind(context.NsRootCtx, nsConst.SETTINGS_NAME);
            settings = (rtSettings) sett_soi.swapInf(rtSettings.infid);
            
            // Retrieve TLS passprase and ask user if not set
            String passphrase="";
            try
            {
                passphrase = settings.getValue( BaseKeyMaterial.SSL_PASSPHRASE_SETTING );
            }
            catch(Exception e)
            {
                throw new RTSException("bkm: cannot get " + BaseKeyMaterial.SSL_PASSPHRASE_SETTING + " runtime setting");
            }
            
            if (passphrase == null || passphrase.equals(""))
            {
                System.out.println("Enter passphrase for client->object server authentication: ");
                int c = 0;
                StringBuffer sb = new StringBuffer();
                while( true )
                {
                    c = System.in.read();
                    if (c == -1 && c == '\n')
                        break;
                    else
                    {
                        sb.append( (char)c );
                    }
                }
                passphrase = sb.toString();
                if (passphrase.equals(""))
                {
                    throw new IOException( "bkm: TLS passphrase required." );
                }
                
                try
                {
                    settings.addSetting( BaseKeyMaterial.SSL_PASSPHRASE_SETTING, passphrase );
                }
                catch(Exception e)
                {
                    throw new RTSException("bkm: cannot set " + BaseKeyMaterial.SSL_PASSPHRASE_SETTING + " runtime setting");
                }
            }
            
        }
        catch( RTSException e )
        {
            throw e;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new RTSException("bkm:cannot bind to settings object: " + e.getMessage() );
        }
        finally
        {
            if (settings != null)
            {
                settings.relInf();
                settings = null;
            }
        }
    }
}
