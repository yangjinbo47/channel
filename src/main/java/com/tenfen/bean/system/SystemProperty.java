package com.tenfen.bean.system;

public class SystemProperty {
	
	private String clientVisitLogDir;//客户端访问日志路径
	private String tySpaceDataIp;//天翼空间数据存放ip
	private String tySpaceDataPort;//天翼空间数据存放port
	private String tySpaceDataUserName;//天翼空间数据存放userName
	private String tySpaceDataPassword;//天翼空间数据存放password
	private String tySpaceDataLocalDir;//天翼空间数据存放本地路径
	private String tySpaceDataRemoteDir;//天翼空间数据存放远程路径
	private String tySpacePackageDataLocalDir;//天翼空间包月包本地路径
	private String tySpacePackageDataRemoteDir;//天翼空间包月包远程路径
	private String unicomDataZxLocalDir;//联通在信数据本地路径
	private String unicomDataZxRemoteDir;//联通在信数据远程路径
	
	private Boolean isSaveToMongo;//是否保存到hbase
	private Boolean isSaveToBeijing;//是否同步到北京

	public String getClientVisitLogDir() {
		return clientVisitLogDir;
	}

	public void setClientVisitLogDir(String clientVisitLogDir) {
		this.clientVisitLogDir = clientVisitLogDir;
	}

	public String getTySpaceDataLocalDir() {
		return tySpaceDataLocalDir;
	}

	public void setTySpaceDataLocalDir(String tySpaceDataLocalDir) {
		this.tySpaceDataLocalDir = tySpaceDataLocalDir;
	}

	public String getTySpaceDataIp() {
		return tySpaceDataIp;
	}

	public void setTySpaceDataIp(String tySpaceDataIp) {
		this.tySpaceDataIp = tySpaceDataIp;
	}

	public String getTySpaceDataPort() {
		return tySpaceDataPort;
	}

	public void setTySpaceDataPort(String tySpaceDataPort) {
		this.tySpaceDataPort = tySpaceDataPort;
	}

	public String getTySpaceDataUserName() {
		return tySpaceDataUserName;
	}

	public void setTySpaceDataUserName(String tySpaceDataUserName) {
		this.tySpaceDataUserName = tySpaceDataUserName;
	}

	public String getTySpaceDataPassword() {
		return tySpaceDataPassword;
	}

	public void setTySpaceDataPassword(String tySpaceDataPassword) {
		this.tySpaceDataPassword = tySpaceDataPassword;
	}

	public String getTySpaceDataRemoteDir() {
		return tySpaceDataRemoteDir;
	}

	public void setTySpaceDataRemoteDir(String tySpaceDataRemoteDir) {
		this.tySpaceDataRemoteDir = tySpaceDataRemoteDir;
	}

	public String getTySpacePackageDataLocalDir() {
		return tySpacePackageDataLocalDir;
	}

	public void setTySpacePackageDataLocalDir(String tySpacePackageDataLocalDir) {
		this.tySpacePackageDataLocalDir = tySpacePackageDataLocalDir;
	}

	public String getTySpacePackageDataRemoteDir() {
		return tySpacePackageDataRemoteDir;
	}

	public void setTySpacePackageDataRemoteDir(String tySpacePackageDataRemoteDir) {
		this.tySpacePackageDataRemoteDir = tySpacePackageDataRemoteDir;
	}

	public String getUnicomDataZxLocalDir() {
		return unicomDataZxLocalDir;
	}

	public void setUnicomDataZxLocalDir(String unicomDataZxLocalDir) {
		this.unicomDataZxLocalDir = unicomDataZxLocalDir;
	}

	public String getUnicomDataZxRemoteDir() {
		return unicomDataZxRemoteDir;
	}

	public void setUnicomDataZxRemoteDir(String unicomDataZxRemoteDir) {
		this.unicomDataZxRemoteDir = unicomDataZxRemoteDir;
	}

	public Boolean getIsSaveToMongo() {
		return isSaveToMongo;
	}

	public void setIsSaveToMongo(Boolean isSaveToMongo) {
		this.isSaveToMongo = isSaveToMongo;
	}

	public Boolean getIsSaveToBeijing() {
		return isSaveToBeijing;
	}

	public void setIsSaveToBeijing(Boolean isSaveToBeijing) {
		this.isSaveToBeijing = isSaveToBeijing;
	}

}
