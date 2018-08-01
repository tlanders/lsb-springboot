package biz.lci.learningspringboot.lsbweb;

import lombok.Data;

@Data
public class Image {
    private int id;
    private String name;

    public Image(int id, String n) {
        this.id = id;
        this.name = n;
    }
}
