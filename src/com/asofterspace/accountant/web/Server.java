/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.accountant.web;

import com.asofterspace.accountant.Database;
import com.asofterspace.accountant.TabCtrl;
import com.asofterspace.accountant.tabs.Tab;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.web.WebServer;
import com.asofterspace.toolbox.web.WebServerRequestHandler;

import java.net.Socket;


public class Server extends WebServer {

	private Database db;

	private Directory serverDir;

	private TabCtrl tabCtrl;


	public Server(Directory webRoot, Directory serverDir, Database db, TabCtrl tabCtrl) {

		super(webRoot, db.getPort());

		this.db = db;

		this.tabCtrl = tabCtrl;

		this.serverDir = serverDir;
	}

	protected WebServerRequestHandler getHandler(Socket request) {
		return new ServerRequestHandler(this, request, webRoot, serverDir, db, tabCtrl);
	}

}
