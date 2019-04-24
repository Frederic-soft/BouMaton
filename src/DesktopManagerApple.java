import java.io.File;
import java.util.List;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class DesktopManagerApple implements AboutHandler, QuitHandler, OpenFilesHandler {
	private BouMaton myApp;
	private boolean handleAbout = false;
	
	public DesktopManagerApple(BouMaton b) {
		Application desk = Application.getApplication();
		myApp = b;

		desk.setAboutHandler(this);
		handleAbout = true;

		desk.setOpenFileHandler(this);

		desk.setQuitHandler(this);
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
