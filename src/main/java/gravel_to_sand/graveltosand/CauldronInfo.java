package gravel_to_sand.graveltosand;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CauldronInfo {
    public static final Logger LOGGER = LoggerFactory.getLogger("meow");


    private int ticks;
    private BlockPos pos;
    private int dimension;

    public CauldronInfo(int ticks, BlockPos pos, int dimension) {
        this.ticks = ticks;
        this.pos = pos;
        this.dimension = dimension;
    }

    @Override
    public boolean equals(Object other){
        if (other instanceof CauldronInfo o){
            return this.pos.equals(o.pos) && this.dimension == o.dimension;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ticks: %d, pos: %s, dimension: %d", ticks, pos, dimension);
    }

    public boolean decrementTicks(){
        return this.ticks-- < 0;
    }
}
