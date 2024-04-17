package com.common.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.os.FileUtils;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.Secure;

public class AKProperty {

    public final static String DSP = "ak_dsp";
    public final static String REVERSE = "ak_reverse";

    public final static String PROPERTY_ILL_STATE = "ak.ill.state";
}
