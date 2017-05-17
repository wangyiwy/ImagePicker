package me.wangyi.imagepicker.model;

import java.util.List;

/**
 * Created on 2017/5/9.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class Folder {
    private String name;
    private String path;
    private Image cover;
    private List<Image> imageList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }

    public int getSize() {
        return imageList == null ? 0 : imageList.size();
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Folder)) {
            return false;
        }

        Folder that = (Folder) obj;

        if (that.getPath() == null || this.getName() == null) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (that.getPath().equals(path) && that.getName().equals(name)) {
            return true;
        }

        return super.equals(obj);
    }
}
