package chalmers.dax021308.ecosystem.controller.mapeditor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import chalmers.dax021308.ecosystem.controller.IController;
import chalmers.dax021308.ecosystem.model.environment.IModel;
import chalmers.dax021308.ecosystem.view.mapeditor.NewMapDialog;

/**
 * Controller class for creating a new map, setting its name.
 * 
 * @author Erik Ramqvist
 *
 */
public class NewMapDialogController implements IController {
	
	private final NewMapDialog view;
	private OnNameSelectedListener listener;
		
	
	private ActionListener finishActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			listener.onSelectedName(view.tbxMapName.getText());
			view.dispose();
		}
	};
	
	public NewMapDialogController(Frame superFrame, OnNameSelectedListener listener) {
		this.listener = listener;
		view = new NewMapDialog(superFrame);
		view.btnCreateNewMap.addActionListener(finishActionListener);
		view.tbxMapName.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
	            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
	            	finishActionListener.actionPerformed(null);
	            } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
	            	release();
	            }
			}
		});
	}
	
	@Override
	public void init() {
		
	}

	@Override
	public void release() {
		view.dispose();
		listener = null;
	}

	@Override
	public void setModel(IModel m) {
		
	}

	public interface OnNameSelectedListener {
		public void onSelectedName(String name);
	}

}
