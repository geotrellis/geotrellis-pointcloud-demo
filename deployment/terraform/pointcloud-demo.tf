#
# ECS Resources
#

# Template for container definition, allows us to inject environment
data "template_file" "ecs_pointcloud_task" {
  template = "${file("${path.module}/task-definitions/pointcloud.json")}"

  vars {
    pc_environment      = "${var.environment}"
    pc_region           = "${var.aws_region}"
    pc_nginx_image      = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/pointcloud-nginx:${var.image_version}"
    pc_api_server_image = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/pointcloud-api-server:${var.image_version}"
  }
}

# Allows resource sharing among multiple containers
resource "aws_ecs_task_definition" "pointcloud" {
  family                = "${var.environment}PointCloud"
  container_definitions = "${data.template_file.ecs_pointcloud_task.rendered}"
}

resource "aws_cloudwatch_log_group" "pointcloud" {
  name              = "log${var.environment}PointCloudDemo"
  retention_in_days = "30"

  tags {
    Environment = "${var.environment}"
  }
}

module "pointcloud_ecs_service" {
  source = "github.com/azavea/terraform-aws-ecs-web-service?ref=0.2.0"

  name                = "PointCloud"
  vpc_id              = "${data.terraform_remote_state.core.vpc_id}"
  public_subnet_ids   = ["${data.terraform_remote_state.core.public_subnet_ids}"]
  access_log_bucket   = "${data.terraform_remote_state.core.logs_bucket_id}"
  access_log_prefix   = "ALB/PointCloud"
  port                = "443"
  ssl_certificate_arn = "${var.ssl_certificate_arn}"

  cluster_name                   = "${data.terraform_remote_state.core.container_instance_name}"
  task_definition_id             = "${aws_ecs_task_definition.pointcloud.family}:${aws_ecs_task_definition.pointcloud.revision}"
  desired_count                  = "${var.pointcloud_ecs_desired_count}"
  min_count                      = "${var.pointcloud_ecs_min_count}"
  max_count                      = "${var.pointcloud_ecs_max_count}"
  deployment_min_healthy_percent = "${var.pointcloud_ecs_deployment_min_percent}"
  deployment_max_percent         = "${var.pointcloud_ecs_deployment_max_percent}"
  container_name                 = "pc-nginx"
  container_port                 = "443"
  ecs_service_role_name          = "${data.terraform_remote_state.core.ecs_service_role_name}"
  ecs_autoscale_role_arn         = "${data.terraform_remote_state.core.ecs_autoscale_role_arn}"

  project     = "Geotrellis PointCloud"
  environment = "${var.environment}"
}
