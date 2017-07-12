package viewer;

import main.AnisotropicDiffusion;
import main.AnisotropicDiffusionNoGrad;
import main.Gaussian;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


/**
 * Created by dibrov on 14/04/17.
 */
public class SwingDislpay {
    private BufferedImage initImg;
    private BufferedImage procImg;
    private LinkedList<Listener> mListenerList;
    private JFrame jf;
    private  JPanel jp;
    private int value = 0;
    private JComponent jInit;
    private JComponent jProc;

    private int sizeX;
    private int sizeY;
    public SwingDislpay(BufferedImage pImg) {
        initImg = pImg;

        sizeX = pImg.getHeight();
        sizeY = pImg.getWidth();
        procImg = new BufferedImage(sizeY, sizeX, BufferedImage.TYPE_BYTE_GRAY);
        initImg.copyData(procImg.getRaster());
        mListenerList = new LinkedList<>();
    }

    public void show() {
        jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(1000, 500);

        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 5, 5);

        jp = new JPanel();

        // fl.setAlignment(FlowLayout.TRAILING);
//        jf.setLayout(fl);
        jp.setLayout(fl);
        jInit = new JPanel()
        {
            private static final long serialVersionUID = 1L;
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                g.drawImage(initImg, 0,0,this);
            }
        };
        jProc = new JPanel()
        {
            private static final long serialVersionUID = 1L;
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                g.drawImage(procImg, 0,0,this);
            }
        };
//        jProc = new JLabel(new ImageIcon(procImg));
        JSlider slider = new JSlider(0, 100, 0);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                value = slider.getValue();
                notifyListeners();
            }
        });

        jInit.setPreferredSize(new Dimension(500,500));
        jProc.setPreferredSize(new Dimension(500,500));

        jp.add(jInit, BorderLayout.CENTER);
        jp.add(jProc, BorderLayout.CENTER);
        jp.add(slider, BorderLayout.CENTER);
        jp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        jf.getContentPane().add(jp);

        jf.pack();
        jf.setVisible(true);

    }


    private int getSliderValue() {
        return value;
    }
    public void setProcImg(BufferedImage newImg) {
        procImg = newImg;
        jProc = new JLabel(new ImageIcon(procImg));
    }
    private void notifyListeners(){
        for (Listener listener: mListenerList) {
            listener.fire();
            jf.repaint();
        }

    }

    private void addListener(Listener l) {
        mListenerList.add(l);
    }

    public static void main(String[] args) {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File("resources/img/noisyWing8bit.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage after = new BufferedImage(2*w, 2*h, BufferedImage.TYPE_BYTE_GRAY);
        AffineTransform at = new AffineTransform();
        at.scale(2.0, 2.0);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(img, after);
        img = after;
//        Gaussian ga = new Gaussian(img, 5,5, 1.5f, 1.5f);
        AnisotropicDiffusionNoGrad ad = new AnisotropicDiffusionNoGrad(img, .6000f,9.5f);
        SwingDislpay sd = new SwingDislpay(img);
        sd.addListener(new Listener() {
            @Override
            public void fire() {

//                ga.setSigma(((float)sd.getSliderValue())/10);
                ad.setSteps(sd.getSliderValue()*10);
                sd.setProcImg(ad.convolve());


            }
        });

        SwingUtilities.invokeLater(()->{sd.show();});
    }
}
