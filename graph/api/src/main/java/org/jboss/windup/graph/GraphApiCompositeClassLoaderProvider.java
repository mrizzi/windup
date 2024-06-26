package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonDependency;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.util.FurnaceCompositeClassLoader;

/**
 * Provides a composite classloader of all addons that depend on the graph addon.
 */
public class GraphApiCompositeClassLoaderProvider {
    private Addon addon;

    private Furnace furnace;

    public GraphApiCompositeClassLoaderProvider() {
        this.furnace = SimpleContainer.getFurnace(GraphApiCompositeClassLoaderProvider.class.getClassLoader());
        for (Addon addon : this.furnace.getAddonRegistry().getAddons()) {
            if (addon.getClassLoader() != null && addon.getClassLoader().equals(GraphApiCompositeClassLoaderProvider.class.getClassLoader())) {
                this.addon = addon;
                break;
            }
        }
    }

    /**
     * Creates a classloader which combines classloaders of all addons depending on Graph API. This insures that
     * FramedGraph can always load all the relevant types of *Model classes (as all model classes will be in Addons that
     * depend on Graph API).
     */
    public ClassLoader getCompositeClassLoader() {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter() {
            @Override
            public boolean accept(Addon addon) {
                return addonDependsOnGraphApi(addon);
            }
        };

        for (Addon addon : furnace.getAddonRegistry().getAddons(filter)) {
            loaders.add(addon.getClassLoader());
        }

        return new FurnaceCompositeClassLoader(getClass().getClassLoader(), loaders);
    }

    private boolean addonDependsOnGraphApi(Addon addon) {
        for (AddonDependency dep : addon.getDependencies()) {
            if (dep.getDependency().equals(this.addon)) {
                return true;
            }
            boolean subDep = addonDependsOnGraphApi(dep.getDependency());
            if (subDep) {
                return true;
            }
        }
        return false;
    }
}
