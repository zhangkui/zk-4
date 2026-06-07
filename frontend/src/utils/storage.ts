const TOKEN_KEY = 'microgrid_token'
const USER_INFO_KEY = 'microgrid_user_info'

export const storage = {
  getToken(): string {
    return localStorage.getItem(TOKEN_KEY) || ''
  },
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
  },
  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
  },
  getUserInfo<T = any>(): T | null {
    const data = localStorage.getItem(USER_INFO_KEY)
    return data ? JSON.parse(data) : null
  },
  setUserInfo(data: any): void {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(data))
  },
  removeUserInfo(): void {
    localStorage.removeItem(USER_INFO_KEY)
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }
}
