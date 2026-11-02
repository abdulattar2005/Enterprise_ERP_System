variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "name" {
  type    = string
  default = "erp"
}

variable "db_password" {
  type      = string
  sensitive = true
}
