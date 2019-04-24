import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.util.List;

public class DesktopManager implements AboutHandler, QuitHandler, OpenFilesHandler {
	private BouMaton myApp;
	private boolean handleAbout = false;
	
	public DesktopManager(BouMaton b) {
		Desktop desk = Desktop.getDesktop();
		myApp = b;
		if (desk.isSupported(Desktop.Action.APP_ABOUT)) {
			desk.setAboutHandler(this);
			handleAbout = true;
		}
		if (desk.isSupported(Desktop.Action.APP_OPEN_FILE)) {
			desk.setOpenFileHandler(this);
		}
		if (desk.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
			desk.setQuitHandler(this);
		}
	}
	
	public boolean getHandleAbout() {
		return handleAbout;
	}
	
	@Override
	public void openFiles(OpenFilesEvent e) {
		List<File> f = e.getFiles();
		if (f.size() > 0) {
			if (f.get(0).canRead()) {
				myApp.readFile(f.get(0));
			}
		}
	}


	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		if (response != null) {
			response.performQuit();
		}
		myApp.doQuit();
	}

	@Override
	public void handleAbout(AboutEvent e) {
		myApp.handleAbout();
	}

}
