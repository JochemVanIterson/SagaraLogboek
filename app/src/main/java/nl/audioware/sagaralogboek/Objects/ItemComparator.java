package nl.audioware.sagaralogboek.Objects;

import java.util.Comparator;

public class ItemComparator implements Comparator<Item> {
    public int compare(Item a, Item b) {
        boolean isInt = false;
        int nameA = 0;
        int nameB = 0;
        try {
            nameA = Integer.parseInt(a.getName());
            nameB = Integer.parseInt(b.getName());
            isInt = true;
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        int itemComparison;
        if(isInt){
            itemComparison = nameA - nameB;
        } else {
            itemComparison = a.getName().compareTo(b.getName());
        }
        int categoryComparison = a.getCategory().getName().compareTo(b.getCategory().getName());

        return categoryComparison == 0 ? itemComparison : categoryComparison;
    }
}
