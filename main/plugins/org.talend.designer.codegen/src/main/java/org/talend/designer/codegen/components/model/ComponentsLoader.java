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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.component_cache.ComponentInfo;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.IComponent;
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
    }

    private void loadComponentProviders() {
        ComponentsProviderManager componentsProviderManager = ComponentsProviderManager.getInstance();
        componentsProviderManager.getProviders().forEach(e -> {
            providers.put(e.getClass().getCanonicalName(), e);
        });
    }

    public Set<IComponent> loadAllComponentsFromIndex() {
        Set<IComponent> retSet = new HashSet<IComponent>();
        if (cacheFromFile == null) {
            return retSet;
        }
        Set<Entry<String, EList<ComponentInfo>>> entries = cacheFromFile.getComponentEntryMap().entrySet();
        for (Entry<String, EList<ComponentInfo>> entry : entries) {
            for (ComponentInfo ci : entry.getValue()) {
                AbstractComponentsProvider provider = providers.get(ci.getProviderClass());
                try {
                    IComponent comp = new EmfComponent(ci.getUriString(), ci.getSourceBundleName(), entry.getKey(),
                            ci.getPathSource(),
                            false, provider);
                    retSet.add(comp);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return retSet;
    }
}
