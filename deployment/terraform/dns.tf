#
# Public DNS resources
#

resource "aws_route53_record" "origin" {
  zone_id = "${data.aws_route53_zone.external.id}"
  name    = "pointcloud-origin.${data.aws_route53_zone.external.name}"
  type    = "A"

  alias {
    name                   = "${lower(module.pointcloud_ecs_service.lb_dns_name)}"
    zone_id                = "${module.pointcloud_ecs_service.lb_zone_id}"
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "cloudfront" {
  zone_id = "${data.aws_route53_zone.external.id}"
  name    = "pointcloud.${data.aws_route53_zone.external.name}"
  type    = "A"

  alias {
    name                   = "${aws_cloudfront_distribution.cdn.domain_name}"
    zone_id                = "${aws_cloudfront_distribution.cdn.hosted_zone_id}"
    evaluate_target_health = false
  }
}
