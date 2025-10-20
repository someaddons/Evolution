package com.evolution.config;

import com.cupboard.config.CupboardConfig;
import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.evolution.Evolution.MOD_ID;

public class CommonConfiguration implements ICommonConfig
{
    public static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());

    public boolean displayTraitsInJadeOrWaila = true;
    public double noTraitWeight = 2.0;
    public int         maxTraits       = 3;
    public Set<String> entityBlackList = new HashSet<>();
    public Set<String> traitBlackList = new HashSet<>();

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Whether display the traits on a mob in jade/waila(has to be installed to show): default:true");
        entry.addProperty("displayTraitsInJadeOrWaila", displayTraitsInJadeOrWaila);
        root.add("displayTraitsInJadeOrWaila", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Set the weight of getting no trait, the higher this is the lower the probability of received a trait for the entity. default:2.0");
        entry2.addProperty("noTraitWeight", noTraitWeight);
        root.add("noTraitWeight", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Set maximum amount of traits that can be rolled. default:3");
        entry3.addProperty("maxTraits", maxTraits);
        root.add("maxTraits", entry3);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:",
            "List of mobs which are not allowed to receive traits, example:  [\"minecraft:zombie\", \"minecraft:creeper\"]");
        final JsonArray list4 = new JsonArray();
        for (final String name : entityBlackList)
        {
            list4.add(name);
        }
        entry4.add("entityBlackList", list4);
        root.add("entityBlackList", entry4);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty("desc:",
            "List of traits which are not allowed to appear, example:  [\"evolution:armored\", \"evolution:mutant\"]");
        final JsonArray list5 = new JsonArray();
        for (final String name : traitBlackList)
        {
            list5.add(name);
        }
        entry5.add("traitBlackList", list5);
        root.add("traitBlackList", entry5);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        displayTraitsInJadeOrWaila = data.get("displayTraitsInJadeOrWaila").getAsJsonObject().get("displayTraitsInJadeOrWaila").getAsBoolean();
        noTraitWeight = data.get("noTraitWeight").getAsJsonObject().get("noTraitWeight").getAsDouble();
        maxTraits = data.get("maxTraits").getAsJsonObject().get("maxTraits").getAsInt();
        entityBlackList = new HashSet<>();
        for (final JsonElement element : data.get("entityBlackList").getAsJsonObject().get("entityBlackList").getAsJsonArray())
        {
            entityBlackList.add(element.getAsString());
        }
        traitBlackList = new HashSet<>();

        for (final JsonElement element : data.get("traitBlackList").getAsJsonObject().get("traitBlackList").getAsJsonArray())
        {
            traitBlackList.add(element.getAsString());
        }
    }
}
