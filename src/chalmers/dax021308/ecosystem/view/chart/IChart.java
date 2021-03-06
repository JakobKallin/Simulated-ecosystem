/**
 * @author Loanne Berggren
 */
package chalmers.dax021308.ecosystem.view.chart;

import java.awt.Component;
import java.awt.image.BufferedImage;

import chalmers.dax021308.ecosystem.view.IView;

/**
 * @author Loanne Berggren
 *
 */
public interface IChart extends IView{
	public Component toComponent();
	public String getTitle();
	public BufferedImage getSnapShot();
}
