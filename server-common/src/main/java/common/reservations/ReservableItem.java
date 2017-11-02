package common.reservations;

public abstract class ReservableItem {

	private int m_nCount;
	private int m_nPrice;
	private int m_nReserved;
	private String m_strLocation;

	public ReservableItem(String location, int count, int price) {
		m_strLocation = location;
		m_nCount = count;
		m_nPrice = price;
		m_nReserved = 0;
	}

	public void setCount(int count) {
		m_nCount = count;
	}

	public int getCount() {
		return m_nCount;
	}

	public void setPrice(int price) {
		m_nPrice = price;
	}

	public int getPrice() {
		return m_nPrice;
	}

	public void setReserved(int r) {
		m_nReserved = r;
	}

	public int getReserved() {
		return m_nReserved;
	}

	public String getLocation() {
		return m_strLocation;
	}

	@Override
	public abstract String toString();

}
