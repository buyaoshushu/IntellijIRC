package net.roarsoftware.rex.anim;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;


public class TransparentPanel extends JPanel {

	private AlphaComposite composite;
	private float transp;
	
	
	public TransparentPanel() {
		this(new FlowLayout(), 1f);
	}

	public TransparentPanel(float transparency) {
		this(new FlowLayout(), transparency);
	}

	public TransparentPanel(LayoutManager layout) {
		this(layout, 1f);
	}

	public TransparentPanel(LayoutManager layout, float transparency) {
		super(layout);
		setTransparency(transparency);
		setOpaque(false);
	}

	public void setTransparency(float transp) {
		if(transp < 0 || transp > 1) {
			throw new IllegalArgumentException("Wrong Transparency "+ transp);
		}
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transp);
		this.transp = transp;
		repaint();
	}
	
	public float getTransparency() {
		return transp;
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(composite);
		super.paint(g);
	}
	
	protected void addImpl(Component comp, Object constraints, int index) {
		if(comp instanceof JComponent) {
			((JComponent) comp).setOpaque(false);
		}
		super.addImpl(comp, constraints, index);
	}
}
