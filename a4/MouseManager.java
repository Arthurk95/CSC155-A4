package a4;

import org.joml.Matrix4f;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseManager implements MouseListener, MouseMotionListener {
    private int lastX = 0;
    private int lastY = 0;
    private boolean toListen = false;
    private boolean draggable, movable;
    private Camera camera;
    private Matrix4f viewMat = new Matrix4f();

    public MouseManager(Camera c, boolean d, boolean m){
        camera = c;
        draggable = d;
        movable = m;
    }

    public Camera getCamera(){return camera;}
    public void toggleListen(){toListen = !toListen;}

    @Override
    public void mouseDragged(MouseEvent e) {
        if(toListen && draggable){

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(toListen && movable){

        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println(e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
