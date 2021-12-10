/*
 *Copyright Regione Piemonte - 2021
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
// ----------------------------------------------------------------------------
// Original Author of file: Pierfrancesco Vallosio
// Purpose of file: this class holds the information needed to configure an
//                  acquisition board
// Change log:
//   2008-01-10: initial version
// ----------------------------------------------------------------------------
// $Id: BoardDescriptor.java,v 1.9 2015/10/15 11:47:01 pfvallosio Exp $
// ----------------------------------------------------------------------------

package it.csi.periferico.boards;

import it.csi.periferico.config.common.ConfigException;
import it.csi.periferico.config.common.ConfigItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the information needed to configure an acquisition board
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 */
public abstract class BoardDescriptor extends ConfigItem {

	private static final long serialVersionUID = 5535226670886975897L;

	private String brand = "";

	private String model = "";

	private String libName = "";

	private String kernelModuleName = "";

	private String driverName = "";

	private String modelId = "";

	private boolean differentialModeSupported = false;

	private List<DriverParamDescriptor> driverParamDescriptors = new ArrayList<DriverParamDescriptor>();

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = trim(brand);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = trim(model);
	}

	public String getLibName() {
		return libName;
	}

	public void setLibName(String libName) {
		this.libName = libName;
	}

	public String getKernelModuleName() {
		return kernelModuleName;
	}

	public void setKernelModuleName(String kernelModuleName) {
		this.kernelModuleName = trim(kernelModuleName);
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String modParams) {
		driverName = trim(modParams);
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public boolean isDifferentialModeSupported() {
		return differentialModeSupported;
	}

	public void setDifferentialModeSupported(boolean differentialModeSupported) {
		this.differentialModeSupported = differentialModeSupported;
	}

	public List<DriverParamDescriptor> getDriverParamDescriptors() {
		return driverParamDescriptors;
	}

	public void setDriverParamDescriptors(
			List<DriverParamDescriptor> driverParamDescriptors) {
		this.driverParamDescriptors = driverParamDescriptors;
	}

	public DriverParamDescriptor getDriverParamDescriptor(String name) {
		if (name == null)
			throw new IllegalArgumentException("Null argument not allowed");
		if (driverParamDescriptors == null)
			return null;
		for (DriverParamDescriptor dp : driverParamDescriptors) {
			if (name.equals(dp.getName()))
				return dp;
		}
		return null;
	}

	public abstract Board newBoard() throws BoardsException;

	@Override
	public void checkConfig() throws ConfigException {
	}

}
