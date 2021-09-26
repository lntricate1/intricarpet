package intricarpet.script;

import carpet.script.EntityEventsGroup.Event;
import carpet.script.value.ValueConversions;
import carpet.script.value.EntityValue;
import carpet.script.value.Value;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Arrays;

public class intricarpetEntityEvents
{
    public static final Event ON_MOVE = new Event("on_move", 3)
    {
        @Override
        public List<Value> makeArgs(Entity entity, Object... providedArgs)
        {
            return Arrays.asList(
                    new EntityValue(entity),
                    ValueConversions.of((Vec3d) providedArgs[0]),
                    ValueConversions.of((Vec3d) providedArgs[1]),
                    ValueConversions.of((Vec3d) providedArgs[2])
            );
        }
    };
}