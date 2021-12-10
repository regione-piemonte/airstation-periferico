/*
 * Copyright Regione Piemonte - 2021
 * SPDX-License-Identifier: EUPL-1.2-or-later
 */
/*
 * ----------------------------------------------------------------------------
 * Original Author of file: Isabella Vespa
 * Purpose of file: shows an icon
 * Change log:
 *   2008-01-10: initial version
 * ----------------------------------------------------------------------------
 * $Id: IconImageBundle.java,v 1.11 2013/06/12 08:10:38 pfvallosio Exp $
 * ----------------------------------------------------------------------------
 */
package it.csi.periferico.ui.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Shows an icon
 * 
 * @author isabella.vespa@csi.it
 * 
 */
public interface IconImageBundle extends ClientBundle {
	/**
	 * Would match the file 'ledgreen.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/ledgreen.png")
	public ImageResource ledGreen();

	/**
	 * Would match the file 'ledred.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/ledred.png")
	public ImageResource ledRed();

	/**
	 * Would match the file 'ledgray.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/ledgray.png")
	public ImageResource ledGray();

	/**
	 * Would match the file 'enabled.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/enabled.png")
	public ImageResource enabled();

	/**
	 * Would match the file 'disabled.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/disabled.png")
	public ImageResource disabled();

	/**
	 * Would match the file 'removed.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/removed.png")
	public ImageResource removed();

	/**
	 * Would match the file 'out_of_order.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/out_of_order.png")
	public ImageResource out_of_order();

	/**
	 * Would match the file 'help.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/help.png")
	public ImageResource help();

	/**
	 * Would match the file 'init_ok.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/init_ok.png")
	public ImageResource init_ok();

	/**
	 * Would match the file 'init_failed.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/init_failed.png")
	public ImageResource init_failed();

	/**
	 * Would match the file 'detected.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/detected.png")
	public ImageResource detected();

	/**
	 * Would match the file 'not_detected.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/not_detected.png")
	public ImageResource not_detected();

	/**
	 * Would match the file 'pre_init.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/pre_init.png")
	public ImageResource pre_init();

	/**
	 * Would match the file 'ledyellow.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/ledyellow.png")
	public ImageResource ledYellow();

	/**
	 * Would match the file 'warning_high.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/warning_high.png")
	public ImageResource warningHigh();

	/**
	 * Would match the file 'warning_low.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/warning_low.png")
	public ImageResource warningLow();

	/**
	 * Would match the file 'alarm_high.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/alarm_high.png")
	public ImageResource alarmHigh();

	/**
	 * Would match the file 'alarm_low.png' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/alarm_low.png")
	public ImageResource alarmLow();

	/**
	 * Would match the file 'loader.gif' located in the package
	 * 'it.csi.periferico.ui.public.images', provided that this package is part
	 * of the module's classpath.
	 */
	@Source("it/csi/periferico/ui/public/images/loader.gif")
	public ImageResource bar();

}
