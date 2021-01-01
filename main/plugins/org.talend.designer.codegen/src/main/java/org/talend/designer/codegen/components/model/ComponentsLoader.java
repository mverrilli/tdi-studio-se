// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.components.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.component_cache.ComponentInfo;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.core.model.components.EmfComponent;

/*
 * Created by bhe on Dec 31, 2020
 */
public class ComponentsLoader {

    private static final Logger LOGGER = Logger.getLogger(ComponentsLoader.class);

    private static final ComponentsLoader INSTANCE = new ComponentsLoader();

    private ComponentsCache cacheFromFile;

    private final Map<String, AbstractComponentsProvider> providers = new HashMap<String, AbstractComponentsProvider>();

    private ComponentsLoader() {
        readCacheFile();
        loadComponentProviders();
    }

    public static ComponentsLoader getInstance() {
        return INSTANCE;
    }

    private void readCacheFile() {
        String installLocation = new Path(Platform.getConfigurationLocation().getURL().getPath()).toFile().getAbsolutePath();
        try {
            cacheFromFile = ComponentsFactory.loadComponentResource(installLocation);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        if (cacheFromFile == null) {
            return;
        }
    }

    private void loadComponentProviders() {
        BundleContext context = null;
        if (Platform.getProduct() != null) {
            final Bundle definingBundle = Platform.getProduct().getDefiningBundle();
            if (definingBundle != null) {
                context = definingBundle.getBundleContext();
            }
        }
        if (context == null) {
            context = CodeGeneratorActivator.getDefault().getBundle().getBundleContext();
        }
        ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin admin = (PackageAdmin) context.getService(sref);

        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        componentsProviderManager.getProviders().forEach(e -> {
            String bundleName;
            if (!e.isCustom()) {
                bundleName = admin.getBundle(e.getClass()).getSymbolicName();
            } else if (e.getComponentsBundle() == null) {
                bundleName = IComponentsFactory.COMPONENTS_LOCATION;
            } else {
                bundleName = e.getComponentsBundle();
            }
            providers.put(bundleName, e);
        });
    }

    public IComponent loadComponentFromIndex(String name, String type) {
        EmfComponent comp = null;

        EList<ComponentInfo> cis = cacheFromFile.getComponentEntryMap().get(name);
        if (cis != null) {
            for (ComponentInfo ci : cis) {
                if (StringUtils.equals(ci.getType(), type)) {
                    AbstractComponentsProvider provider = providers.get(ci.getSourceBundleName());
                    try {
                        comp = new EmfComponent(ci.getUriString(), ci.getSourceBundleName(), name, ci.getPathSource(), false,
                                provider);
                    } catch (BusinessException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return comp;
    }
}
