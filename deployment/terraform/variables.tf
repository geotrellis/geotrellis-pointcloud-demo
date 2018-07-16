variable "environment" {
  default = "Production"
}

variable "remote_state_bucket" {
  type        = "string"
  description = "Core infrastructure config bucket"
}

variable "aws_account_id" {
  default     = "896538046175"
  description = "Geotrellis PointCloud account ID"
}

variable "aws_region" {
  default = "us-east-1"
}

variable "image_version" {
  type        = "string"
  description = "Geotrellis PointCloud API server & Nginx Image versions"
}

variable "cdn_price_class" {
  default = "PriceClass_200"
}

variable "ssl_certificate_arn" {}

variable "pointcloud_ecs_desired_count" {
  default = "1"
}

variable "pointcloud_ecs_min_count" {
  default = "1"
}

variable "pointcloud_ecs_max_count" {
  default = "2"
}

variable "pointcloud_ecs_deployment_min_percent" {
  default = "100"
}

variable "pointcloud_ecs_deployment_max_percent" {
  default = "200"
}
