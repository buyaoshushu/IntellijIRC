package net.roarsoftware.rex.anim;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;


public class FadeEffect {
	
	public static enum FadeType { FADE_IN, FADE_OUT }

	private static class FadeThread extends Thread {

		private static final long waitTimeBetweenSteps = 100;
		
		private TransparentPanel panel;
		private FadeType type;
		private float stepSize;

		public FadeThread(TransparentPanel panel, FadeType type, float stepSize) {
			this.panel = panel;
			this.type = type;
			this.stepSize = stepSize;
		}

		public void run() {
			int target = (type == FadeType.FADE_IN ? 100 : 0);
			int current = (int) (panel.getTransparency() * 100);
			stepSize *= (type == FadeType.FADE_IN ? 100 : -100);
			while((type == FadeType.FADE_IN ? current < target : current > target)) {
				setTransparency(panel, (float) current / 100);
				try {
					Thread.sleep(waitTimeBetweenSteps);
				} catch(InterruptedException e) {
				}
				current += stepSize;
				if((type == FadeType.FADE_IN ? current > target : current < target))
					current = target;
			}
			setTransparency(panel, target);
		}

		private static void setTransparency(final TransparentPanel panel, final float value) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						panel.setTransparency(value);
						panel.repaint();
					}
				});
			} catch(InterruptedException e) {
			} catch(InvocationTargetException e) {
			}
		}
	}
	

	private FadeEffect() {
	}

	/**
	 * Startet einen Thread der ein <tt>TransparentPanel</tt> ein oder aus fadet.
	 * Der <tt>FadeType</tt> gibt an ob ein oder aus gefadet wird, <tt>stepSize</tt> gibt die
	 * Größe der Schritte an, die gemacht werden um zu faden. Je kleiner <tt>stepSize</tt>
	 * desto länger dauert die Animation. <tt>stepSize</tt> muss zwischen 0 und 1 (beide exklusive
	 * liegen). Ein typischer Wert ist z.B. 0.1.
	 * 
	 * @param panel Das Panel
	 * @param type Der <tt>FadeType</tt>
	 * @param stepSize Die Schrittgröße
	 */
	public static Thread startFading(TransparentPanel panel, FadeType type, float stepSize) {
		if(stepSize <= 0 || stepSize >= 1) {
			throw new IllegalArgumentException("Wrong Step Size "+ stepSize);
		}
		FadeThread t = new FadeThread(panel, type, stepSize);
		t.start();
		return t;
	}
}
