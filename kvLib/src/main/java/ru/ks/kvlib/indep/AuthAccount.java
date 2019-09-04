package ru.ks.kvlib.indep;

public class AuthAccount {
	public final static int AUTH_TYPE_UNKNOWN = 0;//без логина, как гость
	public final static int AUTH_TYPE_TV = 1;//как абонент красноярской сети
	public final static int AUTH_TYPE_KS = 2;//как пользователь ks
	public final static int AUTH_TYPE_GUEST = 3;//как неавторизованный пользователь
	public final static int AUTH_TYPE_KS_SOCIAL = 4;//как пользователь ks через социальную сеть
	public final static int CHANNEL = 0;//номер канала

	private static AuthAccount instance = new AuthAccount();

	private AuthAccount() {};

	public static AuthAccount getInstance() {
		if(instance == null) {
			instance = new AuthAccount();
		}
		return instance;
	}

	private int mType = AUTH_TYPE_UNKNOWN;
	private String mLogin = "";
	private String mPassword = "";
	private String mHash = "1";
	private String mTvHash = "1";
	private String mTvChannel = "0";

	private int getType() {
		return mType;
	}

	public String getLogin() {
		return mLogin;
	}

	public String getPassword() {
		return mPassword;
	}

	public String getHash() {
		return mHash;
	}

	public String getTvHash() {
		return mTvHash;
	}

	public String getTvChannel() {
		return mTvChannel;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setLogin(String login) {
		mLogin = login;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public void setHash(String hash) {
		mHash = hash;
	}

	public void setTvHash(String tvHash) {
		mTvHash = tvHash;
	}

	public void setTvChannel(String tvChannel) {
		mTvChannel = tvChannel;
	}

	public boolean isKSAccount() {
		return mType == AuthAccount.AUTH_TYPE_KS
			|| mType == AuthAccount.AUTH_TYPE_KS_SOCIAL;
	}

	public boolean isTVAccount() {
		return isKSAccount() || getType() == AuthAccount.AUTH_TYPE_TV;
	}

	public boolean isSocialNetworkAccount() {
		return mType == AuthAccount.AUTH_TYPE_KS_SOCIAL;
	}

	public boolean isUnknownAccount() {
		return AuthAccount.getInstance().getType() == AuthAccount.AUTH_TYPE_UNKNOWN;
	}
}
