package com.azavea

import java.security.Permission

object SystemExitControl {
  class ExitTrappedException extends SecurityException

  def forbidSystemExitCall() {
    def securityManager = new SecurityManager() {
      override def checkPermission(permission: Permission) =
        if (permission.getName().contains("exitVM")) throw new ExitTrappedException()
    }

    System.setSecurityManager(securityManager)
  }

  def enableSystemExitCall() = System.setSecurityManager(null)
}
