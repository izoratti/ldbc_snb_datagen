package ldbc.snb.datagen.generator;

import ldbc.snb.datagen.objects.Person;

import java.util.ArrayList;

/**
 * Created by aprat on 11/06/15.
 */
public interface KnowsGenerator {
    public void generateKnows( ArrayList<Person> persons, int seed, ArrayList<Float> percentages, int step_index );
}
