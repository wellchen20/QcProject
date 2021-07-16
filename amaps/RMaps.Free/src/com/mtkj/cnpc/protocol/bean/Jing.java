package com.mtkj.cnpc.protocol.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 井信息存储
 * 
 * @author drh
 * 
 */
public class Jing implements Serializable {

	private static final long serialVersionUID = 3705985645930799238L;

	private String jinghao, zuanjing, xiayao, yaoliang, leiguan;
	private List<String> zhayao = new ArrayList<String>();;
	private List<String> leiguanList = new ArrayList<String>();
	
	public String getJinghao() {
		return jinghao;
	}

	public void setJinghao(String jinghao) {
		this.jinghao = jinghao;
	}

	public String getZuanjing() {
		return zuanjing;
	}

	public void setZuanjing(String zuanjing) {
		this.zuanjing = zuanjing;
	}

	public String getXiayao() {
		return xiayao;
	}

	public void setXiayao(String xiayao) {
		this.xiayao = xiayao;
	}

	public String getYaoliang() {
		return yaoliang;
	}

	public void setYaoliang(String yaoliang) {
		this.yaoliang = yaoliang;
	}

	public String getLeiguan() {
		return leiguan;
	}

	public void setLeiguan(String leiguan) {
		this.leiguan = leiguan;
	}

	public List<String> getZhayao() {
		return zhayao;
	}

	public void setZhayao(List<String> zhayao) {
		if (zhayao != null) {
			this.zhayao.clear();
			for (String string : zhayao) {
				this.zhayao.add(string);
			}
		}
	}

	public List<String> getLeiguanList() {
		return leiguanList;
	}

	public void setLeiguanList(List<String> leiguanList) {
		this.leiguanList.clear();
		if (leiguanList != null) {
			for (String string : leiguanList) {
				this.leiguanList.add(string);
			}
		}
	}
}
