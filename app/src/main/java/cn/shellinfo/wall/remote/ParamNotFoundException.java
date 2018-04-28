package cn.shellinfo.wall.remote;

public class ParamNotFoundException extends RuntimeException {	
	private static final long serialVersionUID = 4485834193374495824L;

	public ParamNotFoundException(String name){
		super("param not found:"+name);
	}
	
	public ParamNotFoundException(String name,String type){
		super("param not found:"+name+" for type:"+type);
	}
}
