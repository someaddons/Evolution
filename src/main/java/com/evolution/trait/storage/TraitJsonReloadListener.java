package com.evolution.trait.storage;

import com.evolution.Evolution;
import com.evolution.trait.Traits;
import com.evolution.trait.type.ITraitType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class TraitJsonReloadListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public TraitJsonReloadListener()
    {
        super(GSON, "trait");
    }

    @Override
    protected void apply(
        final Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, final ResourceManager iResourceManager, final ProfilerFiller iProfiler)
    {
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet())
        {
            final ITraitType traitType = Traits.getTraitType(ResourceLocation.fromNamespaceAndPath(Evolution.MOD_ID, entry.getKey().getPath().toString().replace("trait/","")));
            if (traitType == null)
            {
                Evolution.LOGGER.warn("Could not find trait type: "+entry.getKey());
                continue;
            }

            try
            {
                traitType.loadFromJson(entry.getValue().getAsJsonObject());
            }
            catch (Exception e)
            {
                Evolution.LOGGER.warn("Error during loading trait type: "+entry.getKey(), e);
            }
        }
    }
}
